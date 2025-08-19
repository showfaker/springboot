package com.cie.nems.topology.cache.alarm.log;

import java.util.List;
import java.util.Map;

import com.cie.nems.alarm.log.AlarmLogs;

public interface AlarmLogCacheService {

	public int initAlarmStatus(List<Integer> channelIds);

	public Map<Long, AlarmLogs> getPointAlarmStatus(Long pointId);

	public AlarmLogs getPointRuleAlarmStatus(Long pointId, Long ruleId);

	public void setPointRuleAlarmStatus(AlarmLogs log);

	public void removePointRuleAlarmStatus(AlarmLogs log);

	public int getAlarmStatusCacheSize();

}
