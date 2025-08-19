package com.cie.nems.pre;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;

@Service
public class PreProcessRuleServiceImpl implements PreProcessRuleService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PreProcessRuleDao preProcessRuleDao;
	
	@Override
	public List<PointPreprocessRule> getRules(List<Integer> channelIds, List<Long> ruleIds) {
		List<PointPreprocessRule> rules = preProcessRuleDao.getRules(channelIds, ruleIds);
		if (CommonService.isNotEmpty(rules)) {
			for (PointPreprocessRule r : rules) {
				try {
					if (RULE_TYPE_DEAD.equals(r.getRuleType())) {
						r.setLongParam1(Long.valueOf(r.getParam1()));
					} else if (RULE_TYPE_VALUE_RANGE.equals(r.getRuleType())) {
						r.setDoubleParam1(Double.valueOf(r.getParam1()));
						r.setDoubleParam2(Double.valueOf(r.getParam2()));
					} else if (RULE_TYPE_CHANGE_RANGE.equals(r.getRuleType())) {
						r.setLongParam1(Long.valueOf(r.getParam1()));
						r.setDoubleParam2(Double.valueOf(r.getParam2()));
						r.setDoubleParam3(Double.valueOf(r.getParam3()));
					} else if (RULE_TYPE_CUMULATE_RANGE.equals(r.getRuleType())) {
						r.setLongParam1(Long.valueOf(r.getParam1()));
						r.setDoubleParam2(Double.valueOf(r.getParam2()));
						r.setDoubleParam3(Double.valueOf(r.getParam3()));
					} else if (RULE_TYPE_TIME_RANGE.equals(r.getRuleType())) {
						r.setLongParam1(Long.valueOf(r.getParam1()));
						r.setLongParam2(Long.valueOf(r.getParam2()));
					} else if (RULE_TYPE_RATIO_OFFSET.equals(r.getRuleType())) {
						r.setDoubleParam1(Double.valueOf(r.getParam1()));
						r.setDoubleParam2(Double.valueOf(r.getParam2()));
					} else if (RULE_TYPE_RATIO.equals(r.getRuleType())) {
						r.setDoubleParam1(Double.valueOf(r.getParam1()));
					} else if (RULE_TYPE_OFFSET.equals(r.getRuleType())) {
						r.setDoubleParam1(Double.valueOf(r.getParam1()));
					}
				} catch(Exception e ) {
					logger.error("convert string param to number failed! rule: ", r.toString());
				}
			}
		}
		return rules;
	}

	@Override
	public List<PointPreprocessRela> getRelas(List<Integer> channelIds, List<Long> ruleIds, 
			List<Long> pointIds) {
		return preProcessRuleDao.getRelas(channelIds, ruleIds, pointIds);
	}
}
