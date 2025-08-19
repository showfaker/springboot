package com.cie.nems.topology.alarm.offline;

import java.util.Date;

public interface OfflineAlarmService {

	public static final Integer OFFLINE_ALARM_RULE_ID = -1;

	public static final Integer DEFAULT_START_HHMM = 400;
	public static final Integer DEFAULT_END_HHMM = 2200;
	public static final Long DEFAULT_OUTLINE_DURATION = 900000L;	//15分钟
	public static final Long DEFAULT_OFFLINE_DURATION = 1800000L;	//半小时

	public static final Integer COMMU_STATUS_ONLINE = 0;
	public static final Integer COMMU_STATUS_OFFLINE = 1;
	public static final Integer COMMU_STATUS_DEADVALUE = 2;

	public static final String COMMU_STATUS_ONLINE_STR = "0";
	public static final String COMMU_STATUS_OFFLINE_STR = "1";
	public static final String COMMU_STATUS_DEADVALUE_STR = "2";

	/**
	 * 定时任务触发计算通讯状态
	 * @param date 当前时间
	 */
	public void execute(Date time);

}
