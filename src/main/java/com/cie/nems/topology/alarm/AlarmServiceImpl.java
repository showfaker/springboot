package com.cie.nems.topology.alarm;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.cie.nems.alarm.log.AlarmLogs;
import com.cie.nems.alarm.log.AlarmLogService;
import com.cie.nems.alarm.rule.AlarmRule;
import com.cie.nems.alarm.rule.AlarmRuleService;
import com.cie.nems.alarm.rule.AlarmSideCond;
import com.cie.nems.common.CacheConstants;
import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.redis.RedisService;
import com.cie.nems.common.redis.RedisService.Data;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.station.Station;
import com.cie.nems.topology.cache.alarm.log.AlarmLogCacheService;
import com.cie.nems.topology.cache.alarm.rule.AlarmRuleCacheService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.station.StationCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class AlarmServiceImpl implements AlarmService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.alarm:#{false}}")
	private boolean debug;

	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private ExceptionService exceptionService;
	
	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;
	
	@Autowired
	private AlarmRuleCacheService alarmRuleCacheService;

	@Autowired
	private AlarmLogService alarmLogService;
	
	@Autowired
	private RedisService redisService;

	@Autowired
	private AlarmLogCacheService alarmLogCacheService;
	
	@Autowired
	private StationCacheService stationCacheService;
	
	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	private ObjectMapper om = new ObjectMapper();

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) {
		List<String> specialMsgs = new LinkedList<String>();
		Iterator<ConsumerRecord<Integer, String>> it = msgs.iterator();
		while (it.hasNext()) {
			ConsumerRecord<Integer, String> msg = it.next();
			if (msg != null && msg.value() != null && msg.value().startsWith("{") 
					&& msg.value().indexOf(ALARM_MSG_TYPE_COLUMN_NAME) > -1) {
				specialMsgs.add(msg.value());
				it.remove();
			}
		}
		List<PointValueDto> datas = pointValueCacheService.parseMessage(msgs);

		//普通测点值触发的告警
		if (CommonService.isNotEmpty(datas)) {
			alarm(datas);
		}
		//特殊告警
		if (CommonService.isNotEmpty(specialMsgs)) {
			specialAlarms(specialMsgs);
		}
	}

	/**
	 * 获取测点档案，并按通道分开处理
	 */
	private void alarm(List<PointValueDto> datas) {
		Date now = new Date();
		Long monitorDate = CommonService.trunc(now, TimeType.DAY).getTime();
		List<AlarmRule> rules = null;
		Map<String, AlarmLogs> newAlarms = new TreeMap<String, AlarmLogs>();
		Map<String, AlarmLogs> recoverAlarms = new TreeMap<String, AlarmLogs>();
		
		for (PointValueDto data : datas) {
			PointInfoDto point = pointCacheService.getPointByPointId(data.getPid());
			if (point == null) {
				logger.error("pid {} not exists", data.getPid());
				continue;
			}
			
			data.setPoint(point);
			
			Device d = deviceCacheService.getDeviceByDeviceId(point.getDeviceId());
			if (PointConstants.CATE_ID_COMM_STS.equals(point.getCateId())) {
				rules = alarmRuleCacheService.getCommuAlarmRule(d == null ? null : d.getUseFlag());
			} else {
				rules = alarmRuleCacheService.getPointAlarmRules(data.getPid());
				if (CommonService.isEmpty(rules)) {
					continue;
				}
			}
			
			try {
				//告警计算
				int eventCount = alarm(data, rules, newAlarms, recoverAlarms);
				
				if (d != null && eventCount > 0) {
					//如果有触发告警或复归事件，则更新告警统计信息
					deviceAlarmStatistics(d, monitorDate, now);
				}
			} catch (Exception e) {
				logger.error("alarm failed! {}", data, e);
				exceptionService.log(this.getClass().getName() + "-alarm", data, e);
			}
		}
		
		notice(newAlarms, recoverAlarms);
		
		save(newAlarms, recoverAlarms);
	}

	private int alarm(PointValueDto data, List<AlarmRule> rules, Map<String, AlarmLogs> newAlarms, 
			Map<String, AlarmLogs> recoverAlarms) {
		int eventCount = 0;
		Calendar dt = Calendar.getInstance();
		for (AlarmRule rule : rules) {
			//判断有效期
			if (rule.getBeginDate() != null && data.getDt() < rule.getBeginDate().getTime()) continue;
			if (rule.getEndDate() != null && data.getDt() > rule.getEndDate().getTime()) continue;
			//判断有效时段
			dt.setTimeInMillis(data.getDt());
			int hhmm = dt.get(Calendar.HOUR_OF_DAY) * 100 + dt.get(Calendar.MINUTE);
			if (rule.getBeginTime() != null && hhmm < rule.getBeginTime()) continue;
			if (rule.getEndTime() != null && hhmm > rule.getEndTime()) continue;
			
			try {
				eventCount += alarm(data, rule, newAlarms, recoverAlarms);
			} catch(Exception e) {
				logger.error("do alarm failed! data: {}, rule: {}", data.toString(), rule.toString(), e);
				exceptionService.log(this.getClass().getName() + "-doAlarm", 
						data.toString() + ", ruleId: " + rule.getRuleId(), e);
			}
		}
		return eventCount;
	}

	private int alarm(PointValueDto data, AlarmRule rule, Map<String, AlarmLogs> newAlarms, 
			Map<String, AlarmLogs> recoverAlarms) throws Exception {
		if (isAlarm(data, rule)) {
			if (alarmRuleCacheService.isFilters(data.getPoint().getDeviceId(), data.getPoint().getStationId(), 
					System.currentTimeMillis())) {
				//有过滤规则，不告警
				return 0;
			}
			//符合告警条件
			AlarmLogs preAlarm = alarmLogCacheService.getPointRuleAlarmStatus(data.getPid(), rule.getRuleId());
			if (preAlarm == null) {
				//之前未告警，要新增复告警
				AlarmLogs newAlarm = createAlarmLog(data, rule);
				newAlarms.put(newAlarm.getLogId(), newAlarm);
				//更新告警状态
				alarmLogCacheService.setPointRuleAlarmStatus(newAlarm);
				return 1;
			}
		} else {
			//不符合告警条件
			AlarmLogs preAlarm = alarmLogCacheService.getPointRuleAlarmStatus(data.getPid(), rule.getRuleId());
			if (preAlarm != null) {
				//之前已已告警，要复归
				if (preAlarm.getStartTime().getTime() > data.getDt()) {
					//dt 小于之前的告警时间，可能是历史数据补录，这种情况不处理
					logger.error("pre startTime: {} > dt: {}, pid: {}, ruleId: {}",
							preAlarm.getStartTime().getTime(), data.getDt(), data.getPid(), rule.getRuleId());
					exceptionService.log(this.getClass().getName() + "-recoverAlarm", 
							"pre startTime: "+preAlarm.getStartTime().getTime()+" < dt: "+data.getDt()
							+", pid: "+data.getPid()+", ruleId: "+rule.getRuleId());
				} else {
					preAlarm.setEndTime(new Date(data.getDt()));
					recoverAlarms.put(preAlarm.getLogId(), preAlarm);
					//删除告警状态
					alarmLogCacheService.removePointRuleAlarmStatus(preAlarm);
					return 1;
				}
			}
		}
		return 0;
	}

	private boolean isAlarm(PointValueDto data, AlarmRule rule) throws Exception {
		boolean isTrue = compare(data, rule.getRuleId(), rule.getCompareSymbol(), rule.getCompareVal(), 
				rule.getDoubleCompareVal(), rule.getCompareVals(), rule.getDoubleCompareVals());
		String alarmRemainTimeStr = null;
		if (isTrue) {
			if (rule.getDuration() > 0) {
				//对于有持续时长要求的测点，如果符合告警条件，要记录持续符合条件的最初时间
				alarmRemainTimeStr = redisService.hget(Data.POINT_REMAIN_TIME, data.getPoint().getCalcChannel(), 
						CacheConstants.CACHE_ALARM_REMAIN_TIME, data.getPid()+"_"+rule.getRuleId());
				if (StringUtils.isEmpty(alarmRemainTimeStr)) {
					redisService.hset(Data.POINT_REMAIN_TIME, data.getPoint().getCalcChannel(), 
							CacheConstants.CACHE_ALARM_REMAIN_TIME, data.getPid()+"_"+rule.getRuleId(), 
							String.valueOf(data.getDt()));
					if (debug) {
						logger.debug("hset {} : {} - {}", CacheConstants.CACHE_ALARM_REMAIN_TIME, 
								data.getPid()+"_"+rule.getRuleId(), data.getDt());
					}
				}
			}
		} else {
			if (debug) {
				logger.debug("compare false ruleId: {}, {} {} {}/{}", rule.getRuleId(), rule.getCompareVal(), 
						rule.getCompareSymbol(), data.getV(), data.getDv());
			}
			
			if (rule.getDuration() > 0) {
				//对于有持续时长要求的测点，如果不符合告警条件，要将持续时间缓存清空
				redisService.hdel(Data.POINT_REMAIN_TIME, data.getPoint().getCalcChannel(), 
						CacheConstants.CACHE_ALARM_REMAIN_TIME, data.getPid()+"_"+rule.getRuleId());
				if (debug) {
					logger.debug("hdel {} : {}", CacheConstants.CACHE_ALARM_REMAIN_TIME, data.getPid()+"_"+rule.getRuleId());
				}
			}
			
			return false;
		}

		
		if (rule.getDuration() > 0) {
			//要求符合告警条件的持续时间超过duration秒的情况
			Long alarmRemainTime = null;
			if (StringUtils.isNotEmpty(alarmRemainTimeStr)) {
				try {
					alarmRemainTime = Long.valueOf(alarmRemainTimeStr);
				} catch (NumberFormatException e) {
					logger.error("parse alarm remain time failed! pid: {}, ruleId: {}, remainTime: {}, error: {}", 
							data.getPid(), rule.getRuleId(), alarmRemainTimeStr, e.getMessage());
					alarmRemainTime = null;
				}
			}
			if (debug) {
				logger.debug("check duration: {}, remainTime: {}, dt: {}", 
						rule.getDuration(), alarmRemainTimeStr, data.getDt());
			}
			if (alarmRemainTime == null) {
				//之前未记录符合条件开始时间，说明刚刚开始符合告警条件，所以不需要告警
				return false;
			}
			if (data.getDt() - alarmRemainTime < rule.getDuration() * 1000L) {
				//持续时间不够长，不告警
				return false;
			}
		}
		
		if (CommonService.isNotEmpty(rule.getSideConds())) {
			//alarmRrule 和 sideConds 是且的关系，sSideCond之间也是且的关系
			List<Long> pointIds = new ArrayList<Long>(rule.getSideConds().size());
			for (AlarmSideCond cond : rule.getSideConds()) {
				pointIds.add(cond.getPointId());
			}
			Map<Long, PointValueDto> values = pointValueCacheService.getPointCurrValuesByPointIds(
					data.getPoint().getCalcChannel(), pointIds);
			if (CommonService.isEmpty(values)) {
				return false;
			}
			for (AlarmSideCond cond : rule.getSideConds()) {
				PointValueDto condData = values.get(cond.getPointId());
				if (condData == null) {
					if (debug) {
						logger.debug("compare false condId: {}, pid: {}, point value is null", 
								cond.getCondId(), cond.getPointId());
					}
					return false;
				}
				if (!compare(condData, cond.getRuleId(), cond.getCompareSymbol(), cond.getCompareVal(), 
						cond.getDoubleCompareVal(), cond.getCompareVals(), cond.getDoubleCompareVals())) {
					if (debug) {
						logger.debug("compare false condId: {}, pid: {}, {} {} {}/{}", cond.getCondId(), 
								cond.getPointId(), cond.getCompareVal(), cond.getCompareSymbol(), 
								condData.getV(), condData.getDv());
					}
					return false;
				}
			}
		}
		
		//所有条件都符合要求，判定为告警
		return true;
	}

	private boolean compare(PointValueDto data, Long ruleId, String compareSymbol, String compareVal, 
			Double doubleCompareVal, List<String> compareVals, List<Double> doubleCompareVals) {
		if (StringUtils.isEmpty(compareSymbol)) {
			logger.error("compare symbole of alarm rule {} is empty", ruleId);
			return false;
		}
		if (StringUtils.isEmpty(compareVal)) {
			logger.error("compare value of alarm rule {} is empty", ruleId);
			return false;
		}
		if ("=".equals(compareSymbol)) {
			if (doubleCompareVal != null) {
				return doubleCompareVal.equals(data.getDv());
			} else {
				return compareVal.equals(data.getV());
			}
		} else if ("!=".equals(compareSymbol)) {
			if (doubleCompareVal != null) {
				return !doubleCompareVal.equals(data.getDv());
			} else {
				return !compareVal.equals(data.getV());
			}
		} else if (">".equals(compareSymbol)) {
			if (data.getDv() != null && doubleCompareVal != null) {
				return data.getDv() > doubleCompareVal;
			} else {
				return false;
			}
		} else if (">=".equals(compareSymbol)) {
			if (data.getDv() != null && doubleCompareVal != null) {
				return data.getDv() >= doubleCompareVal;
			} else {
				return false;
			}
		} else if ("<".equals(compareSymbol)) {
			if (data.getDv() != null && doubleCompareVal != null) {
				return data.getDv() < doubleCompareVal;
			} else {
				return false;
			}
		} else if ("<=".equals(compareSymbol)) {
			if (data.getDv() != null && doubleCompareVal != null) {
				return data.getDv() <= doubleCompareVal;
			} else {
				return false;
			}
		} else if ("in".equals(compareSymbol)) {
			if (data.getDv() != null && CommonService.isNotEmpty(doubleCompareVals)) {
				return doubleCompareVals.contains(data.getDv());
			} else if (CommonService.isNotEmpty(compareVals)) {
				return compareVals.contains(data.getV());
			} else {
				return false;
			}
		} else if ("not in".equals(compareSymbol)) {
			if (data.getDv() != null && CommonService.isNotEmpty(doubleCompareVals)) {
				return !doubleCompareVals.contains(data.getDv());
			} else if (CommonService.isNotEmpty(compareVals)) {
				return !compareVals.contains(data.getV());
			} else {
				return true;
			}
		}
		return false;
	}

	private int seq = 0;
	private int getSeq() {
		if (seq > 1000) {
			seq = 0;
		}
		return seq++;
	}
	private AlarmLogs createAlarmLog(PointValueDto data, AlarmRule rule) {
		AlarmLogs log = new AlarmLogs();
		log.setLogId(data.getDt()+"_"+data.getPid()+"_"+rule.getRuleId()+"_"+(getSeq()));
		log.setRelaId(null);
		log.setAlarmLevel(rule.getAlarmLevel());
		log.setAlarmType(rule.getAlarmType());
		log.setAlarmSource(AlarmRuleService.ALARM_SOURCE_REAL);
		log.setAlarmAction(AlarmRuleService.ALARM_ACTION_ALARM);
		log.setAlarmText(createAlarmText(rule, data));
		log.setStationId(data.getPoint().getStationId());
		log.setAreaId(data.getPoint().getAreaId());
		log.setDeviceId(data.getPoint().getDeviceId());
		log.setCompId(null);
		log.setPsrId(data.getPoint().getPsrId());
		log.setPointId(data.getPid());
		log.setRuleId(rule.getRuleId());
		log.setAlarmStatus(AlarmRuleService.ALARM_STATUS_ALARM);
		log.setStartTime(new Date(data.getDt()));
		log.setEndTime(null);
		log.setAlarmCheckStatus(AlarmRuleService.ALARM_CHECK_STATUS_UNCHECK);
		return log;
	}

	private String createAlarmText(AlarmRule rule, PointValueDto data) {
		String alarmText = rule.getAlarmText();
		if (StringUtils.isEmpty(alarmText)) {
			return data.getPoint().getPointName();
		}
		if (alarmText.indexOf("{name}") > -1) {
			alarmText = StringUtils.replace(alarmText, "{name}", data.getPoint().getPointName());
		}
		if (alarmText.indexOf("{v}") > -1) {
			alarmText = StringUtils.replace(alarmText, "{v}", data.getV());
		}
		if (alarmText.indexOf("{setv}") > -1) {
			alarmText = StringUtils.replace(alarmText, "{setv}", rule.getCompareVal());
		}
		if (alarmText.indexOf("{ext}") > -1) {
			alarmText = StringUtils.replace(alarmText, "{ext}", data.getExt());
		}
		if (alarmText.indexOf("{action}") > -1) {
			alarmText = StringUtils.replace(alarmText, "{action}", "告警");
		}
		return alarmText;
	}

	private void save(Map<String, AlarmLogs> newAlarms, Map<String, AlarmLogs> recoverAlarms) {
		Calendar c = Calendar.getInstance();
		Date now = new Date();
		if (CommonService.isNotEmpty(newAlarms)) {
			List<AlarmLogs> newRealAlarms = new ArrayList<AlarmLogs>(newAlarms.size());
			List<AlarmLogs> newRecoverAlarms = new ArrayList<AlarmLogs>(newAlarms.size());
			if (CommonService.isNotEmpty(recoverAlarms)) {
				for (AlarmLogs log : newAlarms.values()) {
					AlarmLogs recover = recoverAlarms.get(log.getLogId());
					if (recover != null) {
						//在本批次中已经复归，则不存入alarm_log_real，而是直接存入alarm_log
						log.setAlarmStatus(AlarmRuleService.ALARM_STATUS_RECOVER);
						log.setEndTime(recover.getEndTime());
						c.setTime(log.getStartTime());
						log.setPartitionId(c.get(Calendar.YEAR) * 100 + c.get(Calendar.MONTH) + 1);
						newRecoverAlarms.add(log);
						log.setCreateTime(now);
						log.setUpdateTime(now);
						recoverAlarms.remove(log.getLogId());
					} else {
						log.setPartitionId(0);
						log.setCreateTime(now);
						newRealAlarms.add(log);
					}
				}
			} else {
				for (AlarmLogs log : newAlarms.values()) {
					log.setPartitionId(0);
					log.setCreateTime(now);
					newRealAlarms.add(log);
				}
			}
			if (CommonService.isNotEmpty(newRealAlarms)) {
				alarmLogService.insertAlarmLogs(newRealAlarms);
			}
			if (CommonService.isNotEmpty(newRecoverAlarms)) {
				alarmLogService.insertAlarmLogs(newRecoverAlarms);
			}
		}
		if (CommonService.isNotEmpty(recoverAlarms)) {
			List<AlarmLogs> alarms = new ArrayList<AlarmLogs>(recoverAlarms.size());
			for (AlarmLogs log : recoverAlarms.values()) {
				c.setTime(log.getStartTime());
				log.setPartitionId(c.get(Calendar.YEAR) * 100 + c.get(Calendar.MONTH) + 1);
				alarms.add(log);
			}
			alarmLogService.recoverAlarms(alarms);
		}
	}

	private void notice(Map<String, AlarmLogs> newAlarms, Map<String, AlarmLogs> recoverAlarms) {
		try {
			List<String> notices = new ArrayList<String>(CommonService.getListInitCapacity(newAlarms.size() + recoverAlarms.size()));
			if (CommonService.isNotEmpty(newAlarms)) {
				for (AlarmLogs log : newAlarms.values()) {
					AlarmNoticeDto dto = createNotice(log, AlarmRuleService.ALARM_ACTION_ALARM);
					notices.add(dto.toString());
				}
			}
			if (CommonService.isNotEmpty(recoverAlarms)) {
				for (AlarmLogs log : recoverAlarms.values()) {
					AlarmNoticeDto dto = createNotice(log, AlarmRuleService.ALARM_ACTION_RECOVER);
					notices.add(dto.toString());
				}
			}
			if (notices.size() > 0) {
				long rows = redisService.rpush(Data.ALARM_NOTICE, null, CacheConstants.CACHE_ALARM_NOTICES, notices);
				logger.debug("append {}/{} notice to {}", rows, notices.size(), CacheConstants.CACHE_ALARM_NOTICES);
			}
		} catch(Exception e) {
			logger.error("create notices for {} alarms and {} recovers failed!", 
					newAlarms.size(), recoverAlarms.size(), e);
			exceptionService.log(this.getClass().getName()+"-notice", 
					newAlarms.size()+" alarms and "+recoverAlarms.size()+" recovers", e);
		}
	}

	private AlarmNoticeDto createNotice(AlarmLogs log, String action) {
		AlarmNoticeDto dto = new AlarmNoticeDto();
		dto.setLogId(log.getLogId());
		dto.setStationId(log.getStationId());
		dto.setDeviceId(log.getDeviceId());
		dto.setAlarmLevel(log.getAlarmLevel());
		dto.setAlarmType(log.getAlarmType());
		dto.setAlarmAction(action);
		dto.setAlarmText(log.getAlarmText());
		dto.setStartTime(log.getStartTime());
		dto.setEndTime(log.getEndTime());
		if (log.getStationId() != null) {
			Station station = stationCacheService.getStationByStationId(log.getStationId());
			if (station != null) {
				dto.setStationName(station.getShortName());
				dto.setCustomerId(station.getCustomerId());
			}
		}
		if (log.getDeviceId() != null) {
			Device device = deviceCacheService.getDeviceByDeviceId(log.getDeviceId());
			if (device != null) {
				dto.setDeviceName(device.getDeviceName());
				if (dto.getCustomerId() == null) {
					dto.setCustomerId(device.getCustomerId());
				}
			}
		}
		return dto;
	}

	private void deviceAlarmStatistics(Device d, Long monitorDate, Date now) {
		Map<String, PointInfoDto> points = pointCacheService.getPointsByObjId(d.getPsrId());
		if (CommonService.isEmpty(points)) return;
		
		int realAlarmCount = 0;
		Long lastAlarmTime = null;
		for (PointInfoDto p : points.values()) {
			Map<Long, AlarmLogs> realAlarms = alarmLogCacheService.getPointAlarmStatus(p.getPointId());
			if (CommonService.isEmpty(realAlarms)) continue;
			
			realAlarmCount += realAlarms.size();
			for (AlarmLogs a : realAlarms.values()) {
				if (lastAlarmTime == null || lastAlarmTime < a.getStartTime().getTime()) {
					lastAlarmTime = a.getStartTime().getTime();
				}
			}
		}
		
		Map<String, Object> data = deviceMonitorCacheService.createDeviceMonitorData(monitorDate, d);
		data.put(DeviceMonitorColumn.real_alarms.getName(), realAlarmCount);
		data.put(DeviceMonitorColumn.real_alarms_time.getName(), now.getTime());
		data.put(DeviceMonitorColumn.last_alarm_time.getName(), lastAlarmTime);
		
		String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(d.getCalcChannel());
		try {
			kafkaService.sendMonitor(monitorCenterTopic, data, debug);
		} catch (Exception e) {
			logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(data), e);
		}
	}

	/**
	 * 一些特殊的告警无法通过简单的测点值来生成，都归口到本方法中处理。</br>
	 *  未来根据需要扩展AlarmMsg对象和alarmMsgType
	 * @param msgs
	 */
	private void specialAlarms(List<String> msgs) {
		Date now = new Date();
		Long monitorDate = CommonService.trunc(now, TimeType.DAY).getTime();
		List<AlarmRule> rules = new ArrayList<AlarmRule>(2);
		Map<String, AlarmLogs> newAlarms = new TreeMap<String, AlarmLogs>();
		Map<String, AlarmLogs> recoverAlarms = new TreeMap<String, AlarmLogs>();
		
		for (String msg: msgs) {
			AlarmMsg amsg = null;
			try {
				amsg = om.readValue(msg, new TypeReference<AlarmMsg>() {});
			} catch (Exception e) {
				logger.error("parse msg failed! {} : {}", msg, e.getMessage());
				exceptionService.log(this.getClass().getName() + "-specialAlarm-parseMsg", msg, e);
			}
			if (amsg == null) continue;
			
			if (ALARM_MSG_TYPE_STATION_OUTLINE.equals(amsg.getAlarmMsgType())) {
				stationOutlineAlarms(amsg, rules, newAlarms, recoverAlarms, monitorDate, now);
			}
		}
		
		notice(newAlarms, recoverAlarms);
		
		save(newAlarms, recoverAlarms);
	}

	private void stationOutlineAlarms(AlarmMsg amsg, List<AlarmRule> rules, Map<String, AlarmLogs> newAlarms,
			Map<String, AlarmLogs> recoverAlarms, Long monitorDate, Date now) {
		PointInfoDto point = pointCacheService.getPointByObjIdCateId(amsg.getPsrId(), PointConstants.CATE_ID_STATION_STS);
		if (point == null) {
			logger.error("point {} - {} not exists", amsg.getPsrId(), PointConstants.CATE_ID_STATION_STS);
			return;
		}
		
		PointValueDto data = new PointValueDto();
		data.setPid(point.getPointId());
		data.setV(amsg.getV());
		data.setDv(Double.valueOf(amsg.getV()));
		data.setDt(amsg.getDt());
		data.setExt(amsg.getAlarmText());
		data.setPoint(point);
		
		AlarmRule rule = alarmRuleCacheService.getAlarmRules(amsg.getRuleId());
		if (rule == null) return;
		
		rules.clear();
		rules.add(rule);
		
		try {
			alarm(data, rules, newAlarms, recoverAlarms);
		} catch (Exception e) {
			logger.error("station outline alarm failed! {}", data, e);
			exceptionService.log(this.getClass().getName() + "-alarm", data, e);
		}
	}

}
