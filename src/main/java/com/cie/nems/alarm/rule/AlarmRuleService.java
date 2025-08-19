package com.cie.nems.alarm.rule;

import java.util.List;

public interface AlarmRuleService {
	public static final String ALARM_ACTION_ALARM = "1";
	public static final String ALARM_ACTION_RECOVER = "0";

	public static final String ALARM_STATUS_ALARM = "1";
	public static final String ALARM_STATUS_RECOVER = "0";

	public static final String ALARM_SOURCE_REAL = "01";	//实时告警程序
	public static final String ALARM_SOURCE_OFFLINE = "02";	//离线告警程序
	public static final String ALARM_SOURCE_GROUP = "03";	//分组分析告警程序

	public static final Long ALARM_RULE_ID_OFFLINE = -1L;	//通讯中断告警
	public static final Long ALARM_RULE_ID_OUTLINE_OFFLINE = -3L;	//外线通讯中断告警
	public static final Long ALARM_RULE_ID_POWEROFF = -2L;	//电站断电告警
	public static final Long ALARM_RULE_ID_DISPERSION = -10L;	//离散率分析告警
	
	public static final String ALARM_RULE_STATUS_ACTIVE = "01";
	public static final String ALARM_RULE_STATUS_INACTIVE = "02";

	public static final String ALARM_RELA_STATUS_NORMAL = "0";
	public static final String ALARM_RELA_STATUS_DELETED = "1";

	public static final String ALARM_LEVEL_FAULT = "01";
	public static final String ALARM_LEVEL_ALARM = "02";
	public static final String ALARM_LEVEL_LOG = "03";
	public static final String ALARM_LEVEL_INFO = "04";
	
	public static final String ALARM_RULE_TYPE_YX = "01";
	public static final String ALARM_RULE_TYPE_YC = "02";
	public static final String ALARM_RULE_TYPE_YC_SS = "04";
	public static final String ALARM_RULE_TYPE_YC_S = "05";
	public static final String ALARM_RULE_TYPE_YC_X = "06";
	public static final String ALARM_RULE_TYPE_YC_XX = "07";
	public static final String ALARM_RULE_TYPE_ADVANCE = "03";

	public static final String ALARM_CHECK_STATUS_UNCHECK = "01";	//未处理
	public static final String ALARM_CHECK_STATUS_CHECKED = "02";	//已确认
	public static final String ALARM_CHECK_STATUS_DEFECT = "03";	//已转缺陷
	public static final String ALARM_CHECK_STATUS_AUTO_CHECKED = "04";	//自动确认
	public static final String ALARM_CHECK_STATUS_AUTO_DEFECT = "05";	//自动转缺陷

	public static final String CHECK_TYPE_CHECK = "01";	//普通确认
	public static final String CHECK_TYPE_DEFECT = "02";	//转缺陷

	public List<AlarmRule> getRules(List<Integer> channelIds, List<Long> ruleIds);
	
	public List<AlarmPointRela> getRelas(List<Integer> channelIds, List<Long> ruleIds, List<Long> pointIds);
	
}
