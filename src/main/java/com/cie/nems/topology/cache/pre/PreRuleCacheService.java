package com.cie.nems.topology.cache.pre;

import java.util.List;
import java.util.Map;

import com.cie.nems.pre.PointPreprocessRule;

public interface PreRuleCacheService {

	public int updatePreRules(List<Integer> channelIds, List<Long> ruleIds);

	public int updatePreRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds);

	public List<PointPreprocessRule> getPreprocessRules(Long pointId);

	public Map<Long, PointPreprocessRule> getPreprocessRules(List<Long> ruleIds);

	public Map<String, Integer> getPreRuleCacheSize();

}
