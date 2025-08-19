package com.cie.nems.topology.cache.pre;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.pre.PointPreprocessRela;
import com.cie.nems.pre.PointPreprocessRule;
import com.cie.nems.pre.PreProcessRuleService;

@Service
public class PreRuleCacheServiceImpl implements PreRuleCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PreProcessRuleService preProcessRuleService;
	
	private Map<Long, PointPreprocessRule> ruleIdMap = new ConcurrentHashMap<Long, PointPreprocessRule>();
	private Map<Long, List<PointPreprocessRule>> pointIdMap = new ConcurrentHashMap<Long, List<PointPreprocessRule>>();
	
	@Override
	public int updatePreRules(List<Integer> channelIds, List<Long> ruleIds) {
		List<PointPreprocessRule> rules = preProcessRuleService.getRules(channelIds, ruleIds);
		
		if (CommonService.isEmpty(rules)) return 0;
		
		for (PointPreprocessRule r : rules) {
			ruleIdMap.put(r.getRuleId(), r);
		}
		
		return rules.size();
	}
	
	@Override
	public int updatePreRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds) {
		long t1 = System.currentTimeMillis();
		
		List<PointPreprocessRela> relas = preProcessRuleService.getRelas(channelIds, ruleIds, pointIds);
		
		if (CommonService.isEmpty(relas)) return 0;
		
		for (PointPreprocessRela r : relas) {
			PointPreprocessRule rule = ruleIdMap.get(r.getRuleId());
			if (rule == null) continue;
			List<PointPreprocessRule> rules = pointIdMap.get(r.getPointId());
			if (rules == null) {
				rules = new ArrayList<PointPreprocessRule>(3);
				pointIdMap.put(r.getPointId(), rules);
			}
			rules.add(rule);
		}

		long t2 = System.currentTimeMillis();
		logger.debug("used {} seconds", (1.0 * t2 - t1) / 1000L);
		
		return relas.size();
	}

	@Override
	public List<PointPreprocessRule> getPreprocessRules(Long pointId) {
		return pointIdMap.get(pointId);
	}

	@Override
	public Map<Long, PointPreprocessRule> getPreprocessRules(List<Long> ruleIds) {
		Map<Long, PointPreprocessRule> map = new HashMap<Long, PointPreprocessRule>();
		for (Long id : ruleIds) {
			map.put(id, ruleIdMap.get(id));
		}
		return map;
	}

	@Override
	public Map<String, Integer> getPreRuleCacheSize() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("pointIdMap", pointIdMap.size());
		map.put("ruleIdMap", ruleIdMap.size());
		return map;
	}

}
