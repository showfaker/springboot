package com.cie.nems.common;

import java.util.Date;

/**
 * 统一管理平台级常量
 */
public class Constants {

	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	
	/** 用户某些表中的xxx_tree字段内容的分隔符 */
	public static final String TREE_SEPARATOR = "|";

	public static final String SECURITY_USER_STATUS_VALID = "0";

	public static final String SECURITY_DEFAULT_ROLE_ID = "autoRole";

	public static final String MONGO_DEFAULT_DB = "nems";

	public static final String MONGO_DB_PREFIX = "nems_";

	public static final String DEFAULT_ORG_ID = "0";
	public static final String DEFAULT_ROLE_ID = "0";
	public static final String DEFAULT_USER_ID = "0";

	/** 树形结构默认根节点ID */
	public static final String DEFAULT_ROOT_ID = "0";

	//分页默认值
	public static final String DEFAULT_PAGE = "0";
	public static final String DEFAULT_SIZE = "50";

	public static final int DEFAULT_SYS_PAGESIZE = 20;
	
	/**  */
	public static final String DEFAULT_MODELNO_PREFIX = "$";
	
	/** 前后端CheckBox值传输约定 */
	public static final String CHECKBOX_CHECK = "1";
	public static final String CHECKBOX_UNCHECK = "0";
	/** 前端历史数据select */
	public static final String PROP_TYPE_CALC_PERIOD = "calcPeriod";
	public static final String PROP_TYPE_PV_QUERY_CYCLE_01 = "01";// 实时
	public static final String PROP_TYPE_PV_QUERY_CYCLE_10 = "10";// 小时
	public static final String PROP_TYPE_PV_QUERY_CYCLE_20 = "20";// 日
	public static final String PROP_TYPE_PV_QUERY_CYCLE_30 = "30";// 月
	public static final String PROP_TYPE_PV_QUERY_CYCLE_40 = "40";// 年

	/* 对象类型 */
	/** 部件 */
	public static final String OBJ_TYPE_COMP = "01";
	/** 设备 */
	public static final String OBJ_TYPE_DEVICE = "02";
	/** 设备分组 */
	public static final String OBJ_TYPE_DEVICE_GROUP = "03";
	/** 电站区域 */
	public static final String OBJ_TYPE_AREA = "04";
	/** 电站 */
	public static final String OBJ_TYPE_STATION = "05";
	/** 部门 */
	public static final String OBJ_TYPE_DEPT = "06";
	/** PSR */
	public static final String OBJ_TYPE_PSR = "07";
	/** 型号 */
	public static final String OBJ_TYPE_MODEL = "08";
	/** 类型 */
	public static final String OBJ_TYPE_CLASS = "09";

	/** 2010-01-01 0:0:0:000 */
	public static final Date DATA_BEGIN_DATE = new Date(1262275200000L);
	
	public static final String ORDER_ASC = "asc";
	public static final String ORDER_DESC = "desc";
	
	//记录类型
	public static final String RECORD_TYPE_CUSTOMER = "O01";	//租户
	public static final String RECORD_TYPE_STATION = "O02";	//电站
	public static final String RECORD_TYPE_AREA = "O03";	//电站区域
	public static final String RECORD_TYPE_DEVICE_GROUP = "O04";	//设备分组
	public static final String RECORD_TYPE_DEVICE = "O05";	//设备
	public static final String RECORD_TYPE_COMPONENT = "O06";	//部件
	public static final String RECORD_TYPE_MODEL = "O07";	//型号
	public static final String RECORD_TYPE_CLASS = "O08";	//分类
	public static final String RECORD_TYPE_DEVICE_TYPE = "O09";	//设备类
	public static final String RECORD_TYPE_ALARM = "B01";	//告警
	public static final String RECORD_TYPE_DEFECT = "B02";	//缺陷
	public static final String RECORD_TYPE_PATROL = "B03";	//巡视
	public static final String RECORD_TYPE_MAINTAIN = "B04";	//维护
	public static final String RECORD_TYPE_REFORM = "B05";	//技改
	public static final String RECORD_TYPE_DETECT = "B06";	//检测

	//前端传回后端的时间格式
	public static final String dateFormatYear = "yyyy";
	public static final String dateFormatMonth = "yyyy-MM";
	public static final String dateFormatDay = "yyyy-MM-dd";
	public static final String dateFormatHour = "yyyy-MM-dd HH";
	public static final String dateFormatMinute = "yyyy-MM-dd HH:mm";
	public static final String dateFormatSecond = "yyyy-MM-dd HH:mm:ss";
	public static final String dateFormatMillisecond = "yyyy-MM-dd HH:mm:ss:SSS";
	

	// 任务动作
	public static final String TASK_ACTION_COMPLETE = "complete";
	public static final String TASK_ACTION_CLAIM = "claim";
	public static final String TASK_ACTION_DELEGATE = "delegate";
	public static final String TASK_ACTION_RESOLVE = "resolve";

	// 流程实例动作
	public static final String PROCESS_ACTION_SUSPEND = "suspend";
	public static final String PROCESS_ACTION_ACTIVATE = "activate";
	
//	public static final String KAFKA_TOPIC_FRONTEND_OUT = "my-topic-5";
//	public static final String KAFKA_TOPIC_PREPROCESS_OUT = "my-topic-way-out";
	public static final String KAFKA_TOPIC_JOB_LOG = "nems_job_log";
	public static final String KAFKA_TOPIC_JOB_DETAIL = "nems_job_log_detail";
	public static final String KAFKA_TOPIC_APP_LOG = "nems_app_log";
	public static final String KAFKA_TOPIC_WECHAT = "wechat";
	public static final String KAFKA_TOPIC_EMAIL = "email";
	public static final String KAFKA_TOPIC_E2BOX_LOG = "e2box-log-topic";
	public static final String KAFKA_TOPIC_DEVICE_STATUS = "device-status-topic";

	
	public static String KAFKA_DO_SAVE_SIGNAL = "do save";

	// kafka listener
//	public static final String KAFKA_LISTENER_JOB_LOG = "listener_nems_job_log";
//	public static final String KAFKA_LISTENER_JOB_DETAIL = "listener_nems_job_log_detail";
//	public static final String KAFKA_LISTENER_APP_LOG = "listener_nems_app_log";

	// topo
	public static final String TOPO_ITEM_TYPE_SYMBOLS = "03";
	public static final String TOPO_ITEM_TYPE_COMPONENTS = "04";
	public static final String TOPO_ITEM_TYPE_ASSETS = "05";
	
	//非storm版本计算程序topic定义
	public static final String CALC_TOPIC_FRONTEND_POINT = "sdk-point";
	public static final String CALC_TOPIC_FRONTEND_ALARM = "sdk-alarm";
	public static final String CALC_TOPIC_PRE_TO_CALC = "topic-pre-to-calc";
	public static final String CALC_TOPIC_CALC_TO_SAVE = "topic-calc-to-save";
	public static final String CALC_TOPIC_CALC_TO_ALARM = "topic-calc-to-alarm";
	public static final String CALC_TOPIC_ALARM_TO_SAVE = "topic-alarm-to-save";
	public static final String CALC_TOPIC_CALC_TO_STATIS = "topic-calc-to-statis";
	public static final String CALC_TOPIC_ALARM_TO_STATIS = "topic-alarm-to-statis";
	//非storm版本计算程序listener定义
//	public static final String CALC_LISTENER_ID_FRONTEND_POINT = "listener-frontend-point";
//	public static final String CALC_LISTENER_ID_FRONTEND_ALARM = "listener-frontend-alarm";
//	public static final String CALC_LISTENER_ID_PRE_TO_CALC = "listener-pre-to-calc";
//	public static final String CALC_LISTENER_ID_CALC_TO_SAVE = "listener-calc-to-save";
//	public static final String CALC_LISTENER_ID_CALC_TO_ALARM = "listener-calc-to-alarm";
//	public static final String CALC_LISTENER_ID_ALARM_TO_SAVE = "listener-alarm-to-save";
//	public static final String CALC_LISTENER_ID_CALC_TO_STATIS = "listener-calc-to-statis";
//	public static final String CALC_LISTENER_ID_ALARM_TO_STATIS = "listener-alarm-to-statis";

	//前后端查询时使用的查询周期
	public static final String QUERY_TIME_TYPE_REAL = "real";
	public static final String QUERY_TIME_TYPE_HOUR = "hour";
	public static final String QUERY_TIME_TYPE_DAY = "day";
	public static final String QUERY_TIME_TYPE_MONTH = "month";
	public static final String QUERY_TIME_TYPE_YEAR = "year";
	
	//优先级：数字越小优先级越高
	public static final int PRIORITY_0 = 0;
	public static final int PRIORITY_1 = 1;
	public static final int PRIORITY_2 = 2;
	public static final int PRIORITY_3 = 3;
	public static final int PRIORITY_4 = 4;
	public static final int PRIORITY_5 = 5;
	public static final int PRIORITY_6 = 6;
	public static final int PRIORITY_7 = 7;
	public static final int PRIORITY_8 = 8;
	public static final int PRIORITY_9 = 9;
	
	//response http头 application 文件类型
	public static final String RESPONSE_APPLICATION_X_MSDOWNLOAD = "application/x-msdownload";
	public static final String RESPONSE_APPLICATION_MS_EXCEL = "application/vnd.ms-excel";
	public static final String RESPONSE_APPLICATION_FORM_DATA = "multipart/form-data";
	
	//默认时区
	public static final int DEFAULT_ZONE_OFFSET_HOURS = 8;
}
