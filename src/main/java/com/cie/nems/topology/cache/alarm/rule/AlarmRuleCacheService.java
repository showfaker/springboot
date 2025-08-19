package com.cie.nems.topology.cache.alarm.rule;

import java.util.List;
import java.util.Map;

import com.cie.nems.alarm.filter.AlarmFilter;
import com.cie.nems.alarm.rule.AlarmRule;

public interface AlarmRuleCacheService {

	public int updateAlarmRules(List<Integer> channelIds, List<Long> ruleIds);

	public int updateAlarmRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds);

	public AlarmRule getAlarmRules(Long ruleId);

	public Map<Long, AlarmRule> getAlarmRules(List<Long> ruleIds);

	public List<AlarmRule> getPointAlarmRules(Long pointId);

	public int getPointAlarmRuleCount(Long pointId);

	public List<AlarmRule> getCommuAlarmRule(String useFlag);

	public int updateAlarmFilters(List<Integer> channelIds);
	public List<AlarmFilter> getDeviceAlarmFilters(String deviceId);
	public List<AlarmFilter> getStationAlarmFilters(String stationId);
	public boolean isFilters(String deviceId, String stationId, Long time);

	public Map<String, Integer> getAlarmRuleCacheSize();

	public Map<String, List<AlarmRule>> getStationOutlineRules(String stationId);

}
