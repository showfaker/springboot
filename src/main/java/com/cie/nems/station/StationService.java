package com.cie.nems.station;

import java.util.List;

public interface StationService {
	/** 规划中 */
	public static final String STATION_STATUS_PLANNED = "01";
	/** 建设中 */
	public static final String STATION_STATUS_CONSTRUNCT = "02";
	/** 已并网 */
	public static final String STATION_STATUS_CONNECTED = "03";
	/** 已接入系统 */
	public static final String STATION_STATUS_MONITORED = "04";
	/** 已出售 */
	public static final String STATION_STATUS_SELLED = "05";

	/** 停机 */
	public static final String STATION_RUN_STATUS_STOP = "0";
	/** 运行 */
	public static final String STATION_RUN_STATUS_RUN = "1";

	/** 离线 */
	public static final String STATION_COMM_STATUS_OFFLINE = "0";
	/** 在线 */
	public static final String STATION_COMM_STATUS_ONLINE = "1";

	public static final String POWER_SOURCE_INVERTER = "01";	//逆变器
	public static final String POWER_SOURCE_METER_ACTIVE = "02";	//电表正向
	public static final String POWER_SOURCE_METER_REACTIVE = "03";	//电表反向

	public static final String ENERGY_SOURCE_INVERTER = "01";	//逆变器
	public static final String ENERGY_SOURCE_METER = "02";	//电表
	
	public List<Station> getStations(List<Integer> channelIds);

}
