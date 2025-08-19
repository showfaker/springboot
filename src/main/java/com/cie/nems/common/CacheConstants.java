package com.cie.nems.common;


/**
 * 统一管理所有缓存名称
 */
public class CacheConstants {
	/** 属性列表 */
	public static final String CACHE_KEY_PROP_LIST = "NEMS_CACHE_PROP_LIST";
	/** 数据权限 */
	public static final String CACHE_KEY_DATA_PERMISSION_STATION = "NEMS_CACHE_DATA_PERMISSION_STATION";
	public static final String CACHE_KEY_DATA_PERMISSION_WAREHOUSE = "NEMS_CACHE_DATA_PERMISSION_WAREHOUSE";
	/**
	 * 电站设备状态统计
	 */
	public static final String CACHE_KEY_AREA_DEVICE_STATISTICS = "NEMS_CACHE_AREA_DEVICE_STATISTICS";

	/**
	 * 天气缓存
	 */
	public static final String CACHE_KEY_WEATHER_INFO = "NEMS_CACHE_WEATHER_INFO";
	
	public static final String CACHE_POINT_FORMULA = "NEMS_CACHE_POINT_FORMULA";
	public static final String CACHE_MEASURE_POINT = "NEMS_CACHE_MEASURE_POINT";
	public static final String CACHE_POINT_CURR_VALUE = "NEMS_CACHE_POINT_CURR_VALUE";
	public static final String CACHE_POINT_VALUES = "NEMS_POINT_VALUES";
	public static final String CACHE_POINT_PREPROCESS = "NEMS_CACHE_POINT_PREPROCESS";
	public static final String CACHE_POINT_SAVE_TIME = "NEMS_CACHE_POINT_SAVE_TIME";
	/** 死数规则测点的相同值持续时间 */
	public static final String NEMS_CACHE_POINT_REMAIN_TIME = "NEMS_CACHE_POINT_REMAIN_TIME";
	
	/** 对象告警索引缓存，用于快速查找对象下所含告警的logId */
	public static final String CACHE_KEY_OBJ_ALARM = "NEMS_CACHE_OBJ_ALARM";
	/** 电站和区域的告警统计信息 */
	public static final String CACHE_KEY_OBJ_ALARM_STAT = "NEMS_CACHE_OBJ_ALARM_STAT";
	/** 告警日志缓存，每日定时入库到alarm_log表，若在前端页面执行确认操作，则直接挪到历史表alarm_log_yyyyMM */
	public static final String CACHE_KEY_ALARM_LOG = "NEMS_CACHE_ALARM_LOG";
	/** 已入库，但还暂存在告警日志缓存中的告警记录ID */
	public static final String CACHE_KEY_SAVED_ALARM_LOG = "NEMS_CACHE_SAVED_ALARM_LOG";
	/** 告警alarmSave程序锁 */
	public static final String CACHE_KEY_ALARM_SAVE_LOCK = "NEMS_CACHE_ALARM_SAVE_LOCK";
	/** 告警规则 */
	public static final String CACHE_ALARM_RULE = "NEMS_CACHE_ALARM_RULE";
	/** 告警通知原始队列 */
	public static final String CACHE_ALARM_NOTICES = "NEMS_CACHE_ALARM_NOTICES";
	/** 告警通知规则 */
	public static final String CACHE_ALARM_NOTICE_RULE = "NEMS_CACHE_ALARM_NOTICE_RULE";
	/** 告警状态 */
	public static final String CACHE_ALARM_STATUS = "NEMS_CACHE_ALARM_STATUS";
	/** 符合告警条件持续时间 */
	public static final String CACHE_ALARM_REMAIN_TIME = "NEMS_CACHE_ALARM_REMAIN_TIME";
	/** 告警状态 */
	public static final String CACHE_KEY_DEVICE_STATUS = "NEMS_CACHE_DEVICE_STATUS";

	/** 为了提高查询测点的速度，将分组（包括虚拟分组）的测点放在缓存里 */
	public static final String CACHE_NAMESPACE_DEVICE_GROUP = "NEMS_CACHE_DEVICE_GROUP:";
	public static final String CACHE_DISPERSION_POINT = "NEMS_CACHE_DISPERSION_POINT";

	/** 为了提高查询测点的速度，将每个电站和区域的所有测点、设备的默认测点和列表展示测点存入缓存 */
	public static final String CACHE_KEY_DEVICE_DISPLAY_PID = "NEMS_CACHE_DEVICE_DISPLAY_PID";

	/** 用户最近访问的电站ID列表 */
	public static final String CACHE_KEY_USER_RECENT_STATION = "NEMS_CACHE_USER_RECENT_STATION";

	/** 手机APP最近查询的关键词记录 */
	public static final String CACHE_KEY_APP_SEARCH_HIS = "NEMS_CACHE_APP_SEARCH_HIS";

	@Deprecated
	/** 林洋版本前置机的网关连接状态, 以后合并到NEMS_CACHE_E2BOX中 */
	public static final String CACHE_KEY_E2BOX_CONNECTION = "NEMS_CACHE_E2BOX_CONNECTION";
	@Deprecated
	/** SDK版本前置机的网关连接状态, 以后合并到NEMS_CACHE_E2BOX中 */
	public static final String CACHE_KEY_E2BOX_STATUS = "NEMS_CACHE_E2BOX_STATUS";
	/**
	 * 网关、采集器统一缓存的前缀</br>
	 * NEMS_CACHE_E2BOX:E2BOX:0a17fd01</br>
	 * NEMS_CACHE_E2BOX:LYPMC:01012417
	 */
	public static final String CACHE_KEY_E2BOX = "NEMS_CACHE_E2BOX:";
	/** 电站状态 */
	public static final String CACHE_KEY_STATION_STATUS = "NEMS_CACHE_STATION_STATUS";
	/** 设备状态 */
	public static final String CACHE_KEY_STATION_DEVICE_STATUS = "NEMS_CACHE_STATION_DEVICE_STATUS";
	/** 设备告警状态 */
	public static final String CACHE_KEY_DEVICE_ALARM_STATUS = "NEMS_CACHE_DEVICE_ALARM_STATUS";
	/** APP未读告警消息缓存 */
	public static final String CACHE_KEY_APP_ALARM_NOTICE_UNREAD = "NEMS_CACHE_APP_ALARM_NOTICE_UNREAD";
	/** APP已读告警消息缓存 */
	public static final String CACHE_KEY_APP_ALARM_NOTICE_READED = "NEMS_CACHE_APP_ALARM_NOTICE_READED";
	/** APP未读告警消息缓存 */
	public static final String CACHE_KEY_WEB_ALARM_NOTICE_UNREAD = "NEMS_CACHE_WEB_ALARM_NOTICE_UNREAD";
	/** APP已读告警消息缓存 */
	public static final String CACHE_KEY_WEB_ALARM_NOTICE_READED = "NEMS_CACHE_WEB_ALARM_NOTICE_READED";
	/** 工单seq缓存 */
	public static final String CACHE_KEY_ORDER_CODE_SEQ = "NEMS_CACHE_ORDER_CODE_SEQ";
	/** 工作票seq缓存 */
	public static final String CACHE_KEY_TICKET_CODE_SEQ = "NEMS_CACHE_TICKET_CODE_SEQ";

	/** 设备最近数据更新时间 */
	public static final String CACHE_KEY_DEVICE_UPDATE_TIME = "NEMS_DEVICE_UPDATE_TIME";
	/** 电站最近数据更新时间 */
	public static final String CACHE_KEY_STATION_UPDATE_TIME = "NEMS_STATION_UPDATE_TIME";
	/** 设备最近数据时间 */
	public static final String CACHE_KEY_DEVICE_DATA_TIME = "NEMS_DEVICE_DATA_TIME";
	/** 电站最近数据时间 */
	public static final String CACHE_KEY_STATION_DATA_TIME = "NEMS_STATION_DATA_TIME";

}
