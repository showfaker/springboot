package com.cie.nems.alarm.rule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;

@Service
public class AlarmRuleServiceImpl implements AlarmRuleService {
	
	@Autowired
	private AlarmSiteCondRepository alarmSiteCondRepo;
	
	@Autowired
	private AlarmRuleDao alarmRuleDao;
	
	@Override
	public List<AlarmRule> getRules(List<Integer> channelIds, List<Long> ruleIds) {
		List<AlarmSideCond> conds = alarmSiteCondRepo.findAll();
		Map<Long, List<AlarmSideCond>> ruleConds = new HashMap<Long, List<AlarmSideCond>>();
		if (CommonService.isNotEmpty(conds)) {
			for (AlarmSideCond c : conds) {
				List<AlarmSideCond> list = ruleConds.get(c.getRuleId());
				if (list == null) {
					list = new ArrayList<AlarmSideCond>();
					ruleConds.put(c.getRuleId(), list);
				}
				list.add(c);
				
				if ("in".equals(c.getCompareSymbol()) || "not in".equals(c.getCompareSymbol())) {
					String[] values = StringUtils.split(c.getCompareVal(), ",");
					if (CommonService.isNotEmpty(values)) {
						c.setCompareVals(Arrays.asList(values));
						List<Double> dValues = new ArrayList<Double>();
						for (String v : values) {
							Double dv = null;
							try {
								dv = Double.valueOf(v);
							} catch (NumberFormatException e) {
								dv = null;
							}
							if (dv != null) dValues.add(dv);
						}
						c.setDoubleCompareVals(dValues);
					}
				} else {
					try {
						c.setDoubleCompareVal(Double.valueOf(c.getCompareVal()));
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		
		List<AlarmRule> rules = alarmRuleDao.getRules(channelIds, ruleIds);
		if (CommonService.isNotEmpty(rules)) {
			for (AlarmRule r : rules) {
				r.setSideConds(ruleConds.get(r.getRuleId()));
				
				if (StringUtils.isEmpty(r.getStationId()))
					r.setStationId("0");
				
				if ("in".equals(r.getCompareSymbol()) || "not in".equals(r.getCompareSymbol())) {
					String[] values = StringUtils.split(r.getCompareVal(), ",");
					if (CommonService.isNotEmpty(values)) {
						r.setCompareVals(Arrays.asList(values));
						List<Double> dValues = new ArrayList<Double>();
						for (String v : values) {
							Double dv = null;
							try {
								dv = Double.valueOf(v);
							} catch (NumberFormatException e) {
								dv = null;
							}
							if (dv != null) dValues.add(dv);
						}
						r.setDoubleCompareVals(dValues);
					}
				} else {
					try {
						r.setDoubleCompareVal(Double.valueOf(r.getCompareVal()));
					} catch (NumberFormatException e) {
					}
				}
			}
		}
		return rules;
	}

	@Override
	public List<AlarmPointRela> getRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds) {
		return alarmRuleDao.getRelas(channelIds, ruleIds, pointIds);
	}
}
