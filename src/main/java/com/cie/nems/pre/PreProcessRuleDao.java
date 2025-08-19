package com.cie.nems.pre;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;
import com.cie.nems.common.service.CommonService;

@Component
public class PreProcessRuleDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<PointPreprocessRule> getRules(List<Integer> channelIds, List<Long> ruleIds) {
		String sql = "select rule_id, rule_type, param1, param2, param3, param4, quality "
				+ "from point_preprocess_rule r "
				+ "left join station_calc_param s on r.station_id = s.station_id "
				+ "where r.rule_status = :ruleStatus ";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("ruleStatus", PreProcessRuleService.RULE_STATUS_NORMAL);
		
		if (CommonService.isNotEmpty(channelIds)) {
			sql += "and (r.station_id is null or s.calc_channel in (:channelIds)) ";
			paramMap.put("channelIds", channelIds);
		}
		if (CommonService.isNotEmpty(ruleIds)) {
			sql += "and r.rule_id in (:ruleIds) ";
			paramMap.put("ruleIds", ruleIds);
		}
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, PointPreprocessRule.class);
	}

	public List<PointPreprocessRela> getRelas(List<Integer> channelIds, List<Long> ruleIds, 
			List<Long> pointIds) {
		String sql = "select r.rule_id, r.point_id from point_preprocess_rela r "
				+ "join measure_point p on r.point_id = p.point_id "
				+ "join device d on p.obj_id = d.psr_id "
				+ "join station_calc_param s on d.station_id = s.station_id "
				+ "where r.rela_status = :relaStatus ";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("relaStatus", PreProcessRuleService.RELA_STATUS_NORMAL);
		
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
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, PointPreprocessRela.class);
	}
}
