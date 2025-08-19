package com.cie.nems.common.parameter;

public interface ParamService {

	/** 离线告警程序参数 */
	public static final String PARAM_CODE_OFFLINE_ALARM = "offlineAlarm";
	/** 计算拓扑程序配置 */
	public static final String PARAM_CODE_TOPOLOGY = "nemsTopology";

	public AppParameter getAppParameter(String paramCode);

}
