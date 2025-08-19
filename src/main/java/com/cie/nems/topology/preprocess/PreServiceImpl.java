package com.cie.nems.topology.preprocess;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.cie.nems.common.CacheConstants;
import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.redis.RedisService;
import com.cie.nems.common.redis.RedisService.Data;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.pre.PointPreprocessRule;
import com.cie.nems.pre.PreProcessRuleService;
import com.cie.nems.topology.cache.alarm.rule.AlarmRuleCacheService;
import com.cie.nems.topology.cache.dataTime.DataTimeCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.pre.PreRuleCacheService;
import com.cie.nems.topology.calc.device.DeviceCalcService;
import com.cie.nems.topology.calc.station.StationCalcService;

@Service
@Scope("prototype")
public class PreServiceImpl implements PreService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.pre:#{false}}")
	private boolean debug;

	@Value("${cie.app.debug.expression-calc:#{false}}")
	private boolean expressionDebug;

	@Value("${cie.app.debug.device-calc:#{false}}")
	private boolean deviceDebug;

	@Value("${cie.app.debug.station-calc:#{false}}")
	private boolean stationDebug;

	@Value("${cie.app.debug.alarm:#{false}}")
	private boolean alarmDebug;

	@Value("${cie.app.debug.save:#{false}}")
	private boolean saveDebug;

	@Autowired
	private ExceptionService exceptionService;
	
	@Autowired
	private DataTimeCacheService dataTimeCacheService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private PreRuleCacheService preRuleCacheService;

	@Autowired
	private AlarmRuleCacheService alarmRuleCacheService;
	
	@Autowired
	private RedisService redisService;

	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private DeviceCalcService deviceCalcService;

	@Autowired
	private StationCalcService stationCalcService;

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) {
		List<PointValueDto> datas = pointValueCacheService.parseMessage(msgs);

		if (CommonService.isEmpty(datas)) return;
		
		preprocess(datas);
	}

	/**
	 * 获取测点档案，并按通道分开处理
	 */
	private void preprocess(List<PointValueDto> datas) {
		Map<Integer, List<PointValueDto>> channelDatas = new HashMap<Integer, List<PointValueDto>>();
		for (PointValueDto data : datas) {
			PointInfoDto point = pointCacheService.getPointByPointId(data.getPid());
			if (point == null) {
				logger.error("pid {} not exists", data.getPid());
				continue;
			}
			
			data.setPoint(point);
			
			List<PointValueDto> list = channelDatas.get(point.getCalcChannel());
			if (list == null) {
				list = new ArrayList<PointValueDto>();
				channelDatas.put(point.getCalcChannel(), list);
			}
			list.add(data);
		}
		for (Entry<Integer, List<PointValueDto>> e : channelDatas.entrySet()) {
			try {
				List<PointValueDto> newDatas = new ArrayList<>();	// 存放实时数据
				List<PointValueDto> hisDatas = new ArrayList<>();	// 存放过期的历史数据
				//更新设备/电站数据时间
				updateDataTime(e.getKey(), e.getValue());
				//执行预处理规则
				preprocess(e.getKey(), e.getValue(), newDatas, hisDatas);
				
				if (CommonService.isNotEmpty(newDatas)) {
					//批量更新redis中的测点值
					try {
						pointValueCacheService.updatePointCurrValues(e.getKey(), newDatas);
					} catch (Exception ex) {
						logger.error("update point curr values failed!", ex.getMessage());
						exceptionService.log(this.getClass().getName() + "-updatePointCurrValue", "", ex);
					}
					
					sendPoint(e.getKey(), newDatas, hisDatas);
				}
			} catch (Exception ex) {
				logger.error("proprecess datas for channel {} failed!", e.getKey(), ex);
				exceptionService.log(this.getClass().getName() + "-parseMessage", e.getKey()+" values", ex);
			}
		}
	}

	private void updateDataTime(Integer channel, List<PointValueDto> datas) {
		//数据更新时间
		Map<String, Long> deviceUpdateTime = new HashMap<String, Long>();
		Map<String, Long> stationUpdateTime = new HashMap<String, Long>();
		//最新数据时间
		Map<String, Long> deviceDataTime = new HashMap<String, Long>();
		Map<String, Long> stationDataTime = new HashMap<String, Long>();
		long now = System.currentTimeMillis();

		for (PointValueDto data : datas) {
			PointInfoDto point = data.getPoint();
			
			//更新设备数据时间
			if (point.getDeviceId() != null) {
				if (!deviceUpdateTime.containsKey(point.getDeviceId())) {
					deviceUpdateTime.put(point.getDeviceId(), now);
				}
				deviceDataTime.put(point.getDeviceId(), data.getDt());
			}
			
			//更新电站数据时间
			if (point.getStationId() != null) {
				if (!stationUpdateTime.containsKey(point.getStationId())) {
					stationUpdateTime.put(point.getDeviceId(), now);
				}
				stationDataTime.put(point.getDeviceId(), data.getDt());
			}
		}
		
		//更新设备/电站的数据时间和数据更新时间到缓存
		updateDataTime(channel, now, deviceUpdateTime, stationUpdateTime, 
				deviceDataTime, stationDataTime);
	}

	/**
	 * 处理一个通道的数据
	 */
	private void preprocess(Integer channel, List<PointValueDto> datas, List<PointValueDto> newValues,
							List<PointValueDto> hisValues) throws Exception {
		List<PointPreprocessRule> rules = null;
		
		//批量获取redis中测点当前值
		try {
			pointValueCacheService.getPointPreValues(channel, datas);
		} catch (Exception e) {
			logger.error("get point pre values failed!", e.getMessage());
			exceptionService.log(this.getClass().getName() + "-getPointPreValue", "", e);
			return;
		}
		
		for (PointValueDto data : datas) {
			if (data.getPid() == null) continue;
			
			//预处理
			rules = preRuleCacheService.getPreprocessRules(data.getPid());
			if (CommonService.isNotEmpty(rules)) {
				if (doPreprocess(data, rules)) {
					//将需要更新的测点值加入values，循环结束后批量更新
					addToNewValues(data, newValues, hisValues);
				}
			} else {
				//将需要更新的测点值加入values，循环结束后批量更新
				addToNewValues(data, newValues, hisValues);
			}
		}
	}

	private void updateDataTime(Integer channel, long now, 
			Map<String, Long> deviceUpdateTime, Map<String, Long> stationUpdateTime, 
			Map<String, Long> deviceDataTime, Map<String, Long> stationDataTime) {
		dataTimeCacheService.updateDeviceUpdateTime(channel, deviceUpdateTime);
		dataTimeCacheService.updateDeviceDataTime(channel, deviceDataTime);
		
		dataTimeCacheService.updateStationUpdateTime(channel, stationUpdateTime);
		dataTimeCacheService.updateStationDataTime(channel, stationDataTime);
	}

	private void addToNewValues(PointValueDto data, List<PointValueDto> newValues,
								List<PointValueDto> hisValues) {
		if (data.getDt() == null) return;

		if (data.getPreValue() != null && data.getPreValue().getDt() != null) {
			if (data.getPreValue().getDt() > data.getDt()) {
				//未按时间顺序送到的数据单独放到hisValues里，这批数据不更新redis不触发告警，只用于存放到mongodb
				if (debug) logger.debug("ignore value, pid:{}, preDt: {}, dt: {}", data.getPid(), 
						data.getPreValue().getDt(), data.getDt());
				hisValues.add(data);
			} else if (data.getPreValue().getDt().equals(data.getDt())) {
				//相同时间的数据，看值有没有变化
				if (StringUtils.equals(data.getPreValue().getV(), data.getV())) {
					if (debug) logger.debug("ignore value, pid:{}, dt: {}, preV: {}, v: {}", data.getPid(), 
							data.getDt(), data.getPreValue().getV(), data.getV());
					return;
				}
			}
		}

		newValues.add(data);
	}

	/**
	 * @param data
	 * @param rules
	 * @return true 不丢弃数据, false 丢弃数据
	 */
	private boolean doPreprocess(PointValueDto data, List<PointPreprocessRule> rules) {
		try {
			boolean filter = false;
			PointPreprocessRule offsetRule = null;
			for (PointPreprocessRule rule : rules) {
				if (PreProcessRuleService.RULE_TYPE_DEAD.equals(rule.getRuleType())) {
					//死数
					doDeadRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_VALUE_RANGE.equals(rule.getRuleType())) {
					//合法值范围
					doValueRangeRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_CHANGE_RANGE.equals(rule.getRuleType())) {
					//变化量范围
					doChangeRangeRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_CUMULATE_RANGE.equals(rule.getRuleType())) {
					//电量变化量范围
					doCumulateRangeRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_TIME_RANGE.equals(rule.getRuleType())) {
					//有效时间
					filter = doTimeRangeRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_RATIO_OFFSET.equals(rule.getRuleType())) {
					//系数基值变换
					doRatioOffsetRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_RATIO.equals(rule.getRuleType())) {
					//系数
					doRatioRule(data, rule);
				} else if (PreProcessRuleService.RULE_TYPE_OFFSET.equals(rule.getRuleType())) {
					//偏移量
					offsetRule = rule;
				} else if (PreProcessRuleService.RULE_TYPE_INVERSE.equals(rule.getRuleType())) {
					//遥信遥控取反
					doInverseRule(data, rule);
				}
			}
			
			if (offsetRule != null) {
				//偏移量放在最后执行
				doOffsetRule(data, offsetRule);
			}
			
			if (data.getQ() == null) {
				//未违反任何校验规则，标记为合法数据
				data.setQ(PointConstants.POINT_VALUE_SOURCE_CALC + PointConstants.POINT_VALUE_QUALITY_VALID);
			} else {
				data.setQ(PointConstants.POINT_VALUE_SOURCE_CALC + data.getQ());
			}
			return !filter;
		} catch(Exception e) {
			logger.error("do preprocess failed! data: {}", data.toString(), e);
			exceptionService.log(this.getClass().getName() + "-doPreprocess", data, e);
			//处理失败，丢弃数据
			return false;
		}
	}
 
	private void doDeadRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getPreValue() != null && data.getPreValue().getDv() != null
		 && data.getPreValue().getDv().equals(data.getDv())) {
			String remainTimeStr = redisService.hget(Data.POINT_REMAIN_TIME, data.getPoint().getCalcChannel(), 
					CacheConstants.NEMS_CACHE_POINT_REMAIN_TIME, 
					String.valueOf(data.getPid()));
			if (StringUtils.isNotEmpty(remainTimeStr)) {
				Long remainTime = null;
				try {
					remainTime = Long.valueOf(remainTimeStr);
				} catch (NumberFormatException e) {
					logger.error("parse remain time {} for pid {} failed!", remainTimeStr, data.getPid(), e);
					exceptionService.log(this.getClass().getName() + "-parseRemainTime", data, e);
					remainTime = null;
				}
				if (remainTime != null && (data.getDt() - remainTime) > (rule.getLongParam1() * 1000L)) {
					data.setQ(PointConstants.POINT_VALUE_QUALITY_DEAD);
					if (debug) {
						logger.debug("trigger dead ruleId: {}, pid: {}, dt: {}, remainTime: {}, param1: {}", 
								rule.getRuleId(), data.getPid(), data.getDt(), remainTimeStr, rule.getLongParam1());
					}
				}
			}
		} else {
			redisService.hset(Data.POINT_REMAIN_TIME, data.getPoint().getCalcChannel(), 
					CacheConstants.NEMS_CACHE_POINT_REMAIN_TIME, 
					String.valueOf(data.getPid()), String.valueOf(data.getDt()));
			if (debug) {
				logger.debug("update {}, pid: {}, dt: {}, ruleId: {}", 
						CacheConstants.NEMS_CACHE_POINT_REMAIN_TIME, data.getPid(), data.getDt(), rule.getRuleId());
			}
		}
	}

	private void doValueRangeRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDv() != null) {
			if (rule.getDoubleParam1() != null && data.getDv() < rule.getDoubleParam1()) {
				data.setQ(PointConstants.POINT_VALUE_QUALITY_INVALID_RANGE);
				if (debug) {
					logger.debug("trigger value range ruleId: {}, pid: {}, valied: {} - {}, dv: {}", 
							rule.getRuleId(), data.getPid(), rule.getDoubleParam1(), rule.getDoubleParam2(), data.getDv());
				}
			} else if (rule.getDoubleParam2() != null && data.getDv() > rule.getDoubleParam2()) {
				data.setQ(PointConstants.POINT_VALUE_QUALITY_INVALID_RANGE);
				if (debug) {
					logger.debug("trigger value range ruleId: {}, pid: {}, valied: {} - {}, dv: {}", 
							rule.getRuleId(), data.getPid(), rule.getDoubleParam1(), rule.getDoubleParam2(), data.getDv());
				}
			}
		} else {
			data.setQ(PointConstants.POINT_VALUE_QUALITY_INVALID_RANGE);
			if (debug) {
				logger.debug("trigger value range ruleId: {}, pid: {}, valied: {} - {}, dv: {}", 
						rule.getRuleId(), data.getPid(), rule.getDoubleParam1(), rule.getDoubleParam2(), data.getDv());
			}
		}
	}

	private void doChangeRangeRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDv() != null && data.getDt() != null
		 && data.getPreValue() != null && data.getPreValue().getDv() != null 
		 && data.getPreValue().getDt() != null && data.getDt() != data.getPreValue().getDt()) {
			try {
				double change = ((data.getDv() - data.getPreValue().getDv()) * rule.getLongParam1() * 1000L)
						/ (data.getDt() - data.getPreValue().getDt());
				if (change < rule.getDoubleParam2() || change > rule.getDoubleParam3()) {
					data.setQ(PointConstants.POINT_VALUE_QUALITY_INVALID_CHANGE);
					if (debug) {
						logger.debug("trigger change range ruleId: {}, pid: {}, param1: {}, param2: {}, "
								+ "param3: {}, dv: {}, preDv: {}, dt: {}, preDt: {}", 
								rule.getRuleId(), data.getPid(), rule.getLongParam1(), rule.getDoubleParam2(), 
								rule.getDoubleParam3(), data.getDv(), data.getPreValue().getDv(), 
								data.getDt(), data.getPreValue().getDt());
					}
				}
			} catch (Exception e) {
				logger.error("do change range rule failed! rule: {}, data: {}", rule.toString(), data.toString());
				exceptionService.log(this.getClass().getName() + "-doChangeRangeRule", data, e);
			}
		}
	}

	private void doCumulateRangeRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDv() != null && data.getDt() != null
		 && data.getPreValue() != null && data.getPreValue().getDv() != null 
		 && data.getPreValue().getDt() != null && data.getDt() != data.getPreValue().getDt()) {
			try {
				double change = ((data.getDv() - data.getPreValue().getDv()) * rule.getLongParam1() * 1000L)
						/ (data.getDt() - data.getPreValue().getDt());
				if (change < rule.getDoubleParam2() || change > rule.getDoubleParam3()) {
					data.setQ(PointConstants.POINT_VALUE_QUALITY_INVALID_CUMULATE);
					if (debug) {
						logger.debug("trigger cumulate range ruleId: {}, pid: {}, param1: {}, param2: {}, "
								+ "param3: {}, dv: {}, preDv: {}, dt: {}, preDt: {}", 
								rule.getRuleId(), data.getPid(), rule.getLongParam1(), rule.getDoubleParam2(), 
								rule.getDoubleParam3(), data.getDv(), data.getPreValue().getDv(), 
								data.getDt(), data.getPreValue().getDt());
					}
				}
			} catch (Exception e) {
				logger.error("do cumulate range rule failed! rule: {}, data: {}", rule.toString(), data.toString());
				exceptionService.log(this.getClass().getName() + "-doCumulateRangeRule", data, e);
			}
		}
	}

	/**
	 * @return true 过滤，false 不过滤
	 */
	private boolean doTimeRangeRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDt() != null) {
			Calendar dt = Calendar.getInstance();
			dt.setTimeInMillis(data.getDt());
			int hhmm = dt.get(Calendar.HOUR_OF_DAY) * 100 + dt.get(Calendar.MINUTE);
			if (rule.getLongParam1() != null && hhmm < rule.getLongParam1()) {
				if (debug) {
					logger.debug("trigger time range ruleId: {}, pid: {}, param1: {}, param2: {}, hhmm: {}", 
							rule.getRuleId(), data.getPid(), rule.getLongParam1(), rule.getLongParam2(), hhmm);
				}
				return true;
			}
			if (rule.getLongParam2() != null && hhmm > rule.getLongParam2()) {
				if (debug) {
					logger.debug("trigger time range ruleId: {}, pid: {}, param1: {}, param2: {}, hhmm: {}", 
							rule.getRuleId(), data.getPid(), rule.getLongParam1(), rule.getLongParam2(), hhmm);
				}
				return true;
			}
		}
		return false;
	}

	private void doRatioOffsetRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDv() != null) {
			Double v = data.getDv();
			if (rule.getDoubleParam1() != null) {
				data.setDv(data.getDv() * rule.getDoubleParam1());
			}
			if (rule.getDoubleParam2() != null) {
				data.setDv(data.getDv() + rule.getDoubleParam2());
			}
			data.setIv(data.getDv().intValue());
			data.setV(data.getDv().toString());
			if (debug) {
				logger.debug("trigger ratioOffset ruleId: {}, pid: {}, param1: {}, param2: {}, before: {}, after: dv {} / iv {} / v {}", 
						rule.getRuleId(), data.getPid(), rule.getDoubleParam1(), rule.getDoubleParam2(), 
						v, data.getDv(), data.getIv(), data.getV());
			}
		}
	}

	private void doRatioRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDv() != null && rule.getDoubleParam1() != null) {
			Double v = data.getDv();
			data.setDv(data.getDv() * rule.getDoubleParam1());
			data.setIv(data.getDv().intValue());
			data.setV(data.getDv().toString());
			if (debug) {
				logger.debug("trigger ratio ruleId: {}, pid: {}, param1: {}, before: {}, after: dv {} / iv {} / v {}", 
						rule.getRuleId(), data.getPid(), rule.getDoubleParam1(), 
						v, data.getDv(), data.getIv(), data.getV());
			}
		}
	}

	private void doOffsetRule(PointValueDto data, PointPreprocessRule rule) {
		if (data.getDv() != null && rule.getDoubleParam1() != null) {
			Double v = data.getDv();
			data.setDv(data.getDv() + rule.getDoubleParam1());
			data.setIv(data.getDv().intValue());
			data.setV(data.getDv().toString());
			if (debug) {
				logger.debug("trigger offset ruleId: {}, pid: {}, param1: {}, before: {}, after: dv {} / iv {} / v {}", 
						rule.getRuleId(), data.getPid(), rule.getDoubleParam1(), 
						v, data.getDv(), data.getIv(), data.getV());
			}
		}
	}

	private void doInverseRule(PointValueDto data, PointPreprocessRule rule) {
		//遥信遥控取反，0 -> 1，1 -> 0
		if (data.getIv() != null) {
			Integer v = data.getIv();
			data.setIv(data.getIv() == 0 ? 1 : 0);
			data.setDv(1.0 * data.getIv());
			data.setV(data.getIv().toString());
			if (debug) {
				logger.debug("trigger inverse ruleId: {}, pid: {}, before: {}, after: dv {} / iv {} / v {}", 
						rule.getRuleId(), data.getPid(), v, data.getIv(), data.getDv(), data.getV());
			}
		} else if (data.getDv() != null) {
			Double v = data.getDv();
			data.setDv(data.getDv() == 0.0 ? 1.0 : 0.0);
			data.setIv(data.getDv().intValue());
			data.setV(data.getIv().toString());
			if (debug) {
				logger.debug("trigger inverse ruleId: {}, pid: {}, before: {}, after: dv {} / iv {} / v {}", 
						rule.getRuleId(), data.getPid(), v, data.getIv(), data.getDv(), data.getV());
			}
		}
	}
	
	/**
	 * 根据配置将测点发送到后续拓扑
	 */
	private void sendPoint(Integer channel, List<PointValueDto> datas, List<PointValueDto> hisDatas) throws Exception {
		String saveTopic = kafkaService.getSaveTopicName(channel);
		String alarmTopic = kafkaService.getAlarmTopicName(channel);
		String deviceCalcTopic = kafkaService.getDeviceCalcTopicName(channel);
		String stationCalcTopic = kafkaService.getStationCalcTopicName(channel);
		String expressionCalcTopic = kafkaService.getExpressionCalcTopicName(channel);
		PointInfoDto point = null;
		List<ExpressionDto> expressions = null;
		// 最新实时数据
		for (PointValueDto data : datas) {
			//推送保存入拓扑
			kafkaService.sendPoint(saveTopic, data, saveDebug);
			
			//推送告警拓扑
			if (alarmRuleCacheService.getPointAlarmRuleCount(data.getPid()) > 0
			 || PointConstants.CATE_ID_COMM_STS.equals(data.getPoint().getCateId())) {
				kafkaService.sendPoint(alarmTopic, data, alarmDebug);
			}
			
			//推送公式计算
			expressions = pointCacheService.getRefPointExpressions(data.getPid());
			if (CommonService.isNotEmpty(expressions)) {
				kafkaService.sendPoint(expressionCalcTopic, data, expressionDebug);
			}
			
			//推送设备计算
			point = data.getPoint();
			if (deviceCalcService.isDeviceCalcPoints(point)) {
				kafkaService.sendPoint(deviceCalcTopic, data, deviceDebug);
			} else if (stationCalcService.isStationCalcPoints(point)) {
				kafkaService.sendPoint(stationCalcTopic, data, stationDebug);
			}
		}
		// 过期历史数据
		for (PointValueDto data : hisDatas) {
			// 过期历史数据只做推送保存入拓扑
			kafkaService.sendPoint(saveTopic, data, saveDebug);
		}
	}

}
