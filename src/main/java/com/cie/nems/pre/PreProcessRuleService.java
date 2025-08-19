package com.cie.nems.pre;

import java.util.List;

public interface PreProcessRuleService {

	public static final String RULE_STATUS_NORMAL = "0";
	public static final String RULE_STATUS_DELETED = "1";

	public static final String RELA_STATUS_NORMAL = "0";
	public static final String RELA_STATUS_DELETED = "1";

	public static final String RULE_TYPE_DEAD = "01";			//死数
	public static final String RULE_TYPE_VALUE_RANGE = "02";	//合法值范围
	public static final String RULE_TYPE_CHANGE_RANGE = "03";	//变化量范围
	public static final String RULE_TYPE_CUMULATE_RANGE = "04";	//电量变化量范围
	public static final String RULE_TYPE_TIME_RANGE = "05";		//有效时间
	public static final String RULE_TYPE_RATIO_OFFSET = "06";	//系数基值变换
	public static final String RULE_TYPE_RATIO = "07";			//系数
	public static final String RULE_TYPE_OFFSET = "08";			//偏移量
	public static final String RULE_TYPE_INVERSE = "09";		//遥信遥控取反

	public List<PointPreprocessRule> getRules(List<Integer> channelIds, List<Long> ruleIds);
	
	public List<PointPreprocessRela> getRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds);
	
}
