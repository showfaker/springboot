package com.cie.nems.topology.cache.alarm.log;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.alarm.log.AlarmLogs;
import com.cie.nems.alarm.log.AlarmLogService;
import com.cie.nems.alarm.rule.AlarmRuleService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.topology.alarm.offline.OfflineAlarmService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;

@Service
public class AlarmLogCacheServiceImpl implements AlarmLogCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.alarm:#{false}}")
	private boolean debug;

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;
	
	@Autowired
	private AlarmLogService alarmLogService;
	
	/**
	 * Map(pointId, Map(ruleId, AlarmLog))
	 */
	private Map<Long, Map<Long, AlarmLogs>> alarmStatus = new ConcurrentHashMap<Long, Map<Long, AlarmLogs>>();
	
	@Override
	public int initAlarmStatus(List<Integer> channelIds) {
		long t1 = System.currentTimeMillis();
		
		List<AlarmLogs> logs = alarmLogService.getRealAlarms(channelIds);
		
		if (CommonService.isEmpty(logs)) return 0;
		
		Map<Device, Integer> deviceRealCount = new HashMap<Device, Integer>();
		Map<Device, Long> deviceLastAlarmTime = new HashMap<Device, Long>();
		for (AlarmLogs l : logs) {
			if (l.getPointId() == null) continue;
			
			Map<Long, AlarmLogs> ruleStatus = alarmStatus.get(l.getPointId());
			if (ruleStatus == null) {
				ruleStatus = new ConcurrentHashMap<Long, AlarmLogs>(5);
				alarmStatus.put(l.getPointId(), ruleStatus);
			}
			ruleStatus.put(l.getRuleId(), l);
			
			if (l.getDeviceId() == null) continue;
			Device device = deviceCacheService.getDeviceByDeviceId(l.getDeviceId());
			if (device != null) {
				//根据通讯状态告警标记设备通讯状态
				if (AlarmRuleService.ALARM_RULE_ID_OFFLINE == l.getRuleId()
				 || AlarmRuleService.ALARM_RULE_ID_OUTLINE_OFFLINE == l.getRuleId()) {
					device.setCommuStatus(OfflineAlarmService.COMMU_STATUS_OFFLINE_STR);
				}
				//统计设备实时告警
				Integer alarmCount = deviceRealCount.get(device);
				if (alarmCount == null) {
					alarmCount = 1;
				} else {
					++alarmCount;
				}
				deviceRealCount.put(device, alarmCount);
				
				Long lastAlarmTime = deviceLastAlarmTime.get(device);
				if (lastAlarmTime == null) {
					lastAlarmTime = l.getStartTime().getTime();
				} else if (lastAlarmTime < l.getStartTime().getTime()) {
					lastAlarmTime = l.getStartTime().getTime();
				}
				deviceLastAlarmTime.put(device, lastAlarmTime);
			}
		}
		
		Date now = new Date();
		Long monitorDate = CommonService.trunc(now, TimeType.DAY).getTime();
		for (Entry<Device, Integer> e : deviceRealCount.entrySet()) {
			Device d = e.getKey();
			Map<String, Object> data = deviceMonitorCacheService.createDeviceMonitorData(monitorDate, d);
			data.put(DeviceMonitorColumn.real_alarms.getName(), e.getValue());
			data.put(DeviceMonitorColumn.real_alarms_time.getName(), now.getTime());
			data.put(DeviceMonitorColumn.last_alarm_time.getName(), deviceLastAlarmTime.get(d));
			
			deviceMonitorCacheService.update(d.getDeviceId(), monitorDate, data, now.getTime());
		}
		logger.info("update {} device_monitor_real cache alarm infos", deviceRealCount.size());
		
		long t2 = System.currentTimeMillis();
		logger.debug("used {} seconds", (1.0 * t2 - t1) / 1000L);
		
		return logs.size();
	}
	
	@Override
	public Map<Long, AlarmLogs> getPointAlarmStatus(Long pointId) {
		return alarmStatus.get(pointId);
	}

	@Override
	public AlarmLogs getPointRuleAlarmStatus(Long pointId, Long ruleId) {
		Map<Long, AlarmLogs> ruleStatus = alarmStatus.get(pointId);
		return ruleStatus == null ? null : ruleStatus.get(ruleId);
	}

	@Override
	public void setPointRuleAlarmStatus(AlarmLogs log) {
		Map<Long, AlarmLogs> ruleStatus = alarmStatus.get(log.getPointId());
		if (ruleStatus == null) {
			ruleStatus = new HashMap<Long, AlarmLogs>();
			alarmStatus.put(log.getPointId(), ruleStatus);
		}
		ruleStatus.put(log.getRuleId(), log);
		if (debug) {
			logger.debug("set alarm status pid: {}, ruleId: {}, logId: {}, startTime: {}",
					log.getPointId(), log.getRuleId(), log.getLogId(), log.getStartTime().getTime());
		}
	}

	@Override
	public void removePointRuleAlarmStatus(AlarmLogs log) {
		Map<Long, AlarmLogs> ruleStatus = alarmStatus.get(log.getPointId());
		if (ruleStatus != null) {
			AlarmLogs pre = ruleStatus.remove(log.getRuleId());
			if (debug) {
				logger.debug("remove alarm status pid: {}, ruleId: {}, logId: {}, startTime: {}, endTime: {}, pre exists: {}",
						log.getPointId(), log.getRuleId(), log.getLogId(), log.getStartTime().getTime(), 
						log.getEndTime().getTime(), (pre != null));
			}
		} else if (debug) {
			logger.debug("remove alarm status pid: {}, ruleId: {}, logId: {}, startTime: {}, endTime: {}, no ruleMap",
					log.getPointId(), log.getRuleId(), log.getLogId(), log.getStartTime().getTime(), 
					log.getEndTime().getTime());
		}
	}

	@Override
	public int getAlarmStatusCacheSize() {
		return alarmStatus.size();
	}

}
