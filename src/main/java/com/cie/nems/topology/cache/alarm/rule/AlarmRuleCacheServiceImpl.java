package com.cie.nems.topology.cache.alarm.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.alarm.filter.AlarmFilter;
import com.cie.nems.alarm.filter.AlarmFilterService;
import com.cie.nems.alarm.rule.AlarmPointRela;
import com.cie.nems.alarm.rule.AlarmRule;
import com.cie.nems.alarm.rule.AlarmRuleService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.DeviceService;
import com.cie.nems.topology.alarm.AlarmService;

@Service
public class AlarmRuleCacheServiceImpl implements AlarmRuleCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.alarm:#{false}}")
	private boolean debug;

	@Autowired
	private AlarmRuleService alarmRuleService;
	
	@Autowired
	private AlarmFilterService alarmFilterService;
	
	private Map<Long, AlarmRule> ruleIdMap = new ConcurrentHashMap<Long, AlarmRule>();
	private Map<Long, List<AlarmRule>> pointIdMap = new ConcurrentHashMap<Long, List<AlarmRule>>();
	private Map<String, List<AlarmRule>> stationOutlineMap = new ConcurrentHashMap<String, List<AlarmRule>>();
	
	@Override
	public int updateAlarmRules(List<Integer> channelIds, List<Long> ruleIds) {
		List<AlarmRule> rules = alarmRuleService.getRules(channelIds, ruleIds);
		
		if (CommonService.isEmpty(rules)) return 0;
		
		for (AlarmRule r : rules) {
			ruleIdMap.put(r.getRuleId(), r);
			
			if (AlarmService.ALARM_SOURCE_OFFLINE.equals(r.getAlarmSource())) {
				r.setAlarmText("{ext}");
				List<AlarmRule> list = stationOutlineMap.get(r.getStationId());
				if (list == null) {
					list = new LinkedList<AlarmRule>();
					stationOutlineMap.put(r.getStationId(), list);
				}
				list.add(r);
			}
		}
		
		return rules.size();
	}
	
	@Override
	public int updateAlarmRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds) {
		long t1 = System.currentTimeMillis();
		
		List<AlarmPointRela> relas = alarmRuleService.getRelas(channelIds, ruleIds, pointIds);
		
		if (CommonService.isEmpty(relas)) return 0;
		
		for (AlarmPointRela r : relas) {
			AlarmRule rule = ruleIdMap.get(r.getRuleId());
			if (rule == null) continue;
			List<AlarmRule> rules = pointIdMap.get(r.getPointId());
			if (rules == null) {
				rules = new ArrayList<AlarmRule>(3);
				pointIdMap.put(r.getPointId(), rules);
			}
			rules.add(rule);
		}
		
		long t2 = System.currentTimeMillis();
		logger.debug("used {} seconds", (1.0 * t2 - t1) / 1000L);
		
		return relas.size();
	}

	@Override
	public AlarmRule getAlarmRules(Long ruleId) {
		return ruleIdMap.get(ruleId);
	}

	@Override
	public Map<Long, AlarmRule> getAlarmRules(List<Long> ruleIds) {
		Map<Long, AlarmRule> map = new HashMap<Long, AlarmRule>();
		for (Long id : ruleIds) {
			map.put(id, ruleIdMap.get(id));
		}
		return map;
	}
	
	@Override
	public List<AlarmRule> getPointAlarmRules(Long pointId) {
		return pointIdMap.get(pointId);
	}

	@Override
	public int getPointAlarmRuleCount(Long pointId) {
		List<AlarmRule> rules = pointIdMap.get(pointId);
		return rules == null ? 0 : rules.size();
	}

	List<AlarmRule> outlineCommuAlarmRules = null;
	List<AlarmRule> commuAlarmRules = null;
	
	@Override
	public List<AlarmRule> getCommuAlarmRule(String useFlag) {
		if (DeviceService.USE_FLAG_OUTLINE.equals(useFlag)) {
			if (outlineCommuAlarmRules == null) {
				outlineCommuAlarmRules = Arrays.asList(getAlarmRules(AlarmRuleService.ALARM_RULE_ID_OUTLINE_OFFLINE));
			}
			if (outlineCommuAlarmRules == null) {
				outlineCommuAlarmRules = new ArrayList<AlarmRule>();
				
				AlarmRule rule = new AlarmRule();
				rule.setRuleId(AlarmRuleService.ALARM_RULE_ID_OFFLINE);
				rule.setRuleType(AlarmRuleService.ALARM_RULE_TYPE_YX);
				rule.setRuleType(AlarmRuleService.ALARM_SOURCE_REAL);
				rule.setAlarmLevel(AlarmRuleService.ALARM_LEVEL_FAULT);
				rule.setAlarmType("11");
				rule.setAlarmText("外线设备通讯中断告警");
				rule.setCompareSymbol("!=");
				rule.setCompareVal("0");
				rule.setDoubleCompareVal(0.0);
				rule.setHasSideCond(false);
				rule.setAlarmCheckType(AlarmRuleService.CHECK_TYPE_CHECK);
				Calendar c = Calendar.getInstance();
				c = CommonService.trunc(c, TimeType.YEAR);
				c.set(Calendar.YEAR, 2000);
				rule.setBeginDate(c.getTime());
				c.set(Calendar.YEAR, 2099);
				rule.setEndDate(c.getTime());
				rule.setBeginTime(0);
				rule.setEndTime(2359);
				
				outlineCommuAlarmRules.add(rule);
			}
			return commuAlarmRules;
		} else {
			if (commuAlarmRules == null) {
				commuAlarmRules = Arrays.asList(getAlarmRules(AlarmRuleService.ALARM_RULE_ID_OFFLINE));
			}
			if (commuAlarmRules == null) {
				commuAlarmRules = new ArrayList<AlarmRule>();
				
				AlarmRule rule = new AlarmRule();
				rule.setRuleId(AlarmRuleService.ALARM_RULE_ID_OFFLINE);
				rule.setRuleType(AlarmRuleService.ALARM_RULE_TYPE_YX);
				rule.setRuleType(AlarmRuleService.ALARM_SOURCE_REAL);
				rule.setAlarmLevel(AlarmRuleService.ALARM_LEVEL_FAULT);
				rule.setAlarmType("05");
				rule.setAlarmText("设备通讯中断告警");
				rule.setCompareSymbol("!=");
				rule.setCompareVal("0");
				rule.setDoubleCompareVal(0.0);
				rule.setHasSideCond(false);
				rule.setAlarmCheckType(AlarmRuleService.CHECK_TYPE_CHECK);
				Calendar c = Calendar.getInstance();
				c = CommonService.trunc(c, TimeType.YEAR);
				c.set(Calendar.YEAR, 2000);
				rule.setBeginDate(c.getTime());
				c.set(Calendar.YEAR, 2099);
				rule.setEndDate(c.getTime());
				rule.setBeginTime(0);
				rule.setEndTime(2359);
				
				commuAlarmRules.add(rule);
			}
			return commuAlarmRules;
		}
	}

	private Map<String, List<AlarmFilter>> deviceFilters = new ConcurrentHashMap<String, List<AlarmFilter>>();
	private Map<String, List<AlarmFilter>> stationFilters = new ConcurrentHashMap<String, List<AlarmFilter>>();
	
	@Override
	public int updateAlarmFilters(List<Integer> channelIds) {
		List<AlarmFilter> filters = alarmFilterService.getAlarmFilters(channelIds);
		if (CommonService.isNotEmpty(filters)) {
			for (AlarmFilter f : filters) {
				if (StringUtils.isNotEmpty(f.getDeviceId())) {
					List<AlarmFilter> list = deviceFilters.get(f.getDeviceId());
					if (list == null) {
						list = new ArrayList<AlarmFilter>();
						deviceFilters.put(f.getDeviceId(), list);
					}
					list.add(f);
				} else if (StringUtils.isNotEmpty(f.getStationId())) {
					List<AlarmFilter> list = stationFilters.get(f.getStationId());
					if (list == null) {
						list = new ArrayList<AlarmFilter>();
						stationFilters.put(f.getStationId(), list);
					}
					list.add(f);
				}
			}
			return filters.size();
		}
		return 0;
	}
	
	@Override
	public List<AlarmFilter> getDeviceAlarmFilters(String deviceId) {
		return deviceFilters.get(deviceId);
	}

	@Override
	public List<AlarmFilter> getStationAlarmFilters(String stationId) {
		return stationFilters.get(stationId);
	}

	@Override
	public boolean isFilters(String deviceId, String stationId, Long time) {
		if (StringUtils.isNotEmpty(deviceId)) {
			List<AlarmFilter> filters = getDeviceAlarmFilters(deviceId);
			if (CommonService.isNotEmpty(filters)) {
				for (AlarmFilter f : filters) {
					if (f.getStartTime() != null && f.getStartTime().getTime() > time) {
						continue;
					}
					if (f.getEndTime() != null && f.getEndTime().getTime() < time) {
						continue;
					}
					if (debug) {
						logger.debug("alarm filter by filterId: {}", f.getFilterId());
					}
					return true;
				}
			}
		}
		if (StringUtils.isNotEmpty(stationId)) {
			List<AlarmFilter> filters = getStationAlarmFilters(stationId);
			if (CommonService.isNotEmpty(filters)) {
				for (AlarmFilter f : filters) {
					if (f.getStartTime() != null && f.getStartTime().getTime() > time) {
						continue;
					}
					if (f.getEndTime() != null && f.getEndTime().getTime() < time) {
						continue;
					}
					if (debug) {
						logger.debug("alarm filter by filterId: {}", f.getFilterId());
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Map<String, Integer> getAlarmRuleCacheSize() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("pointIdMap", pointIdMap.size());
		map.put("ruleIdMap", ruleIdMap.size());
		map.put("stationOutlineMap", stationOutlineMap.size());
		return map;
	}

	@Override
	public Map<String, List<AlarmRule>> getStationOutlineRules(String stationId) {
		if (StringUtils.isEmpty(stationId)) {
			return stationOutlineMap;
		} else {
			Map<String, List<AlarmRule>> map = new HashMap<String, List<AlarmRule>>();
			map.put(stationId, stationOutlineMap.get(stationId));
			return map;
		}
	}

}
