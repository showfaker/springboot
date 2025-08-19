package com.cie.nems.topology.calc.expression;

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

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.common.util.SpringContextUtil;
import com.cie.nems.device.Device;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.point.expression.TargetPointDto;
import com.cie.nems.station.Station;
import com.cie.nems.topology.cache.alarm.rule.AlarmRuleCacheService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.station.StationCacheService;
import com.cie.nems.topology.calc.device.DeviceCalcService;
import com.cie.nems.topology.calc.station.StationCalcService;

@Service
@Scope("prototype")
public class ExpressionServiceImpl implements ExpressionService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.expression-calc:#{false}}")
	private boolean debug;

	@Autowired
	private ExceptionService exceptionService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private AlarmRuleCacheService alarmRuleCacheService;

	@Autowired
	private DeviceCalcService deviceCalcService;

	@Autowired
	private StationCalcService stationCalcService;

	@Autowired
	private StationCacheService stationCacheService;
	
	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) {
		List<PointValueDto> datas = pointValueCacheService.parseMessage(msgs);

		if (CommonService.isEmpty(datas)) return;
		
		calc(datas);
	}

	private void calc(List<PointValueDto> datas) {
		Map<Integer, List<PointValueDto>> channelDatas = new HashMap<Integer, List<PointValueDto>>();
		Map<Integer, List<Map<String, Object>>> monitorDatas = new HashMap<Integer, List<Map<String, Object>>>();
		for (PointValueDto data : datas) {
			List<ExpressionDto> expressions = pointCacheService.getRefPointExpressions(data.getPid());
			if (CommonService.isNotEmpty(expressions)) {
				for (ExpressionDto exp : expressions) {
					List<PointValueDto> results = calc(data, exp, monitorDatas);
					
					if (CommonService.isNotEmpty(results)) {
						for (PointValueDto result : results) {
							PointInfoDto point = pointCacheService.getPointByPointId(result.getPid());
							if (point == null) {
								logger.error("pid {} not exists", result.getPid());
								continue;
							}
							result.setPoint(point);
							
							List<PointValueDto> list = channelDatas.get(point.getCalcChannel());
							if (list == null) {
								list = new ArrayList<PointValueDto>();
								channelDatas.put(point.getCalcChannel(), list);
							}
							list.add(result);
						}
					}
				}
			}
		}
		
		for (Entry<Integer, List<PointValueDto>> e : channelDatas.entrySet()) {
			if (CommonService.isNotEmpty(e.getValue())) {
				//批量更新redis中的测点值
				try {
					pointValueCacheService.updatePointCurrValues(e.getKey(), e.getValue());
				} catch (Exception ex) {
					logger.error("update point curr values failed!", ex.getMessage());
					exceptionService.log(this.getClass().getName() + "-updatePointCurrValues", "", ex);
				}
				
				try {
					sendPoint(e.getKey(), e.getValue());
				} catch (Exception ex) {
					logger.error("send expression result datas for channel {} failed!", e.getKey(), ex);
					exceptionService.log(this.getClass().getName() + "-sendPoint", "", ex);
				}
			}
		}
		
		for (Entry<Integer, List<Map<String, Object>>> e : monitorDatas.entrySet()) {
			String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(e.getKey());
			for (Map<String, Object> monitorData : e.getValue()) {
				try {
					kafkaService.sendMonitor(monitorCenterTopic, monitorData, debug);
				} catch (Exception ex) {
					logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(monitorData), ex);
				}
			}
		}
	}

	private List<PointValueDto> calc(PointValueDto data, ExpressionDto exp, Map<Integer, List<Map<String, Object>>> monitorDatas) {
		if (exp == null) {
			logger.error("exp is null");
			return null;
		}
		if (data == null) {
			logger.error("data is null");
			return null;
		}
		if (debug) {
			logger.debug("calc {} for data {}", exp.toString(), data.toString());
		}
		
		if (CommonService.isEmpty(exp.getTargetPoints())) {
			logger.error("targetPoints is empty, ownPointId: {}", exp.getOwnPointId());
			return null;
		}
		
		//检查计算时间间隔，未到计算间隔不计算
		if (exp.getCalcInterval() != null && exp.getCalcInterval() > 0L
		 && exp.getLastCalcTime() != null) {
			long duration = data.getDt() - exp.getLastCalcTime();
			if (duration < exp.getCalcInterval() && duration > -exp.getCalcInterval()) {
				if (debug) {
					logger.debug("skip, duration: {}", duration);
				}
				return null;
			}
		}
		
		//查找对应的计算实现类执行计算
		ExpressionCalcService service = null;
		try {
			service = (ExpressionCalcService) SpringContextUtil.getBean(exp.getExpression());
		} catch (Exception e) {
			logger.error("get expression class {} failed!", exp.getExpression(), e);
			return null;
		}
		List<PointValueDto> results = service.calc(data, exp, monitorDatas);
		
		//更新最近计算时间
		if (exp.getLastCalcTime() == null || data.getDt() > exp.getLastCalcTime()) {
			exp.setLastCalcTime(data.getDt());
		}
		
		return results;
	}

	private void sendPoint(Integer channel, List<PointValueDto> datas) throws Exception {
		String saveTopic = kafkaService.getSaveTopicName(channel);
		String alarmTopic = kafkaService.getAlarmTopicName(channel);
		String deviceCalcTopic = kafkaService.getDeviceCalcTopicName(channel);
		String stationCalcTopic = kafkaService.getStationCalcTopicName(channel);
		String expressionCalcTopic = kafkaService.getExpressionCalcTopicName(channel);
		List<ExpressionDto> expressions = null;

		for (PointValueDto data : datas) {
			//推送保存入拓扑
			kafkaService.sendPoint(saveTopic, data, debug);
			
			//推送告警拓扑
			if (alarmRuleCacheService.getPointAlarmRuleCount(data.getPid()) > 0
			 || PointConstants.CATE_ID_COMM_STS.equals(data.getPoint().getCateId())) {
				kafkaService.sendPoint(alarmTopic, data, debug);
			}
			
			//推送公式计算
			expressions = pointCacheService.getRefPointExpressions(data.getPid());
			if (CommonService.isNotEmpty(expressions)) {
				kafkaService.sendPoint(expressionCalcTopic, data, debug);
			}
			
			if (deviceCalcService.isDeviceCalcPoints(data.getPoint())) {
				kafkaService.sendPoint(deviceCalcTopic, data, debug);
			} else if (stationCalcService.isStationCalcPoints(data.getPoint())) {
				kafkaService.sendPoint(stationCalcTopic, data, debug);
			}
		}
	}

	@Override
	public List<PointValueDto> updatePointValues(PointValueDto data, ExpressionDto exp, TargetPointDto target,
			Double value, Map<Integer, List<Map<String, Object>>> monitorDatasMap) {
		List<PointValueDto> results = new ArrayList<PointValueDto>(2);
		
		PointInfoDto point = pointCacheService.getPointByPointId(target.getPointId());
		if (point == null) {
			logger.error("pid {} in expression(targetPointId: {}).targetPoints not exists", 
					target.getPointId(), exp.getOwnPointId());
		} else {
			//Long _id, Long pid, String v, Long dt, Long t, Integer q
			PointValueDto result = new PointValueDto(null, target.getPointId(), 
					value.toString(), data.getDt(), null, 
					PointConstants.POINT_VALUE_SOURCE_CALC + PointConstants.POINT_VALUE_QUALITY_VALID);
			results.add(result);
			exp.setLastCalcTime(data.getDt());
			
			if (StringUtils.isNotEmpty(target.getMonitorTable()) && StringUtils.isNotEmpty(target.getMonitorColumn())) {
				if ("stationMonitorReal".equals(target.getMonitorTable())) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(data.getDt());
					
					Station station = stationCacheService.getStationByStationId(point.getStationId());
					if (station == null) {
						logger.error("stationId {} not exists!", point.getStationId());
					} else {
						Map<String, Object> monitorData = stationMonitorCacheService.createStationMonitorData(
							CommonService.trunc(c, TimeType.DAY).getTimeInMillis(), station);
						monitorData.put(target.getMonitorColumn(), value);
						
						List<Map<String, Object>> monitorDatas = monitorDatasMap.get(station.getCalcChannel());
						if (monitorDatas == null) {
							monitorDatas = new ArrayList<Map<String, Object>>();
							monitorDatasMap.put(station.getCalcChannel(), monitorDatas);
						}
						monitorDatas.add(monitorData);
					}
				} else if ("deviceMonitorReal".equals(target.getMonitorTable())) {
					Calendar c = Calendar.getInstance();
					c.setTimeInMillis(data.getDt());
					
					Device device = deviceCacheService.getDeviceByDeviceId(point.getDeviceId());
					if (device == null) {
						logger.error("deviceId {} not exists!", point.getDeviceId());
					} else {
						Map<String, Object> monitorData = deviceMonitorCacheService.createDeviceMonitorData(
							CommonService.trunc(c, TimeType.DAY).getTimeInMillis(), device);
						monitorData.put(target.getMonitorColumn(), value);
						
						List<Map<String, Object>> monitorDatas = monitorDatasMap.get(device.getCalcChannel());
						if (monitorDatas == null) {
							monitorDatas = new ArrayList<Map<String, Object>>();
							monitorDatasMap.put(device.getCalcChannel(), monitorDatas);
						}
						monitorDatas.add(monitorData);
					}
				} 
			}
		}
		return results;
	}

}
