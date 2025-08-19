package com.cie.nems.alarm.rule;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;
import com.cie.nems.common.service.CommonService;

@Component
public class AlarmRuleDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<AlarmRule> getRules(List<Integer> channelIds, List<Long> ruleIds) {
		String sql = "select * from alarm_rule r "
				+ "left join station_calc_param s on r.station_id = s.station_id "
				+ "where r.rule_status = :ruleStatus "
				+ "and r.rule_type <> :ruleType ";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ruleStatus", AlarmRuleService.ALARM_RULE_STATUS_ACTIVE);
		paramMap.put("ruleType", AlarmRuleService.ALARM_RULE_TYPE_YC);
		
		if (CommonService.isNotEmpty(channelIds)) {
			sql += "and (r.station_id is null or s.calc_channel in (:channelIds)) ";
			paramMap.put("channelIds", channelIds);
		}
		if (CommonService.isNotEmpty(ruleIds)) {
			sql += "and r.rule_id in (:ruleIds) ";
			paramMap.put("ruleIds", ruleIds);
		}
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, AlarmRule.class);
	}

	public List<AlarmPointRela> getRelas(List<Integer> channelIds, List<Long> ruleIds, 
			List<Long> pointIds) {
		String sql = "select r.point_id, r.rule_id from alarm_point_rela r "
				+ "join station_calc_param s on r.station_id = s.station_id "
				+ "where r.rela_status = :relaStatus ";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("relaStatus", AlarmRuleService.ALARM_RELA_STATUS_NORMAL);
		
		if (CommonService.isNotEmpty(channelIds)) {
			sql += "and s.calc_channel in (:channelIds) ";
			paramMap.put("channelIds", channelIds);
		}
		if (CommonService.isNotEmpty(ruleIds)) {
			sql += "and r.rule_id in (:ruleIds) ";
			paramMap.put("ruleIds", ruleIds);
		}
		if (CommonService.isNotEmpty(pointIds)) {
			sql += "and r.point_id in (:pointIds) ";
			paramMap.put("pointIds", pointIds);
		}
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, AlarmPointRela.class);
	}
}
