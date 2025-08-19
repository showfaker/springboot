package com.cie.nems.point;

import java.util.Arrays;
import java.util.List;

public interface PointConstants {
	/**
	 * 由于device和component对象既可以自己直接关联测点，也可以通过PSR关联测点，
	 * 所以查询时需要明确查询范围：
	 * psrPoints: 查询对象当前关联的PSR对象的测点（默认）,
	 * objPoints: 查询对象自己直接关联的测点, 
	 * allPoints: 查询以上两者的合集
	 */
	public static enum PointRang {
		psrPoints, objPoints, allPoints
	};
	
	/** only support number value */
	public static final int POINT_VALUE_FORMAT_ONLY_NUM = 1;
	/** support all data types */
	public static final int POINT_VALUE_FORMAT_ALL = 2;

	public static final String POINT_VALUE_DATABASE_HBASE = "H";
	public static final String POINT_VALUE_DATABASE_MONGODB = "M";
	public static final String POINT_VALUE_DATABASE_POSTGRE = "P";

	/** 侧点至合并方法：求和 */
	public static final String MERGE_TYPE_SUM = "sum";
	/** 侧点至合并方法：平均（当测点值为空或非法时，不参与计算） */
	public static final String MERGE_TYPE_AVG1 = "avg1";
	/** 侧点至合并方法：平均（当测点值为空或非法时，也参与计算） */
	public static final String MERGE_TYPE_AVG2 = "avg2";
	/** 侧点至合并方法：加权平均（当测点值为空或非法时，不参与计算） */
	public static final String MERGE_TYPE_WAVG1 = "wavg1";
	/** 侧点至合并方法：加权平均（当测点值为空或非法时，也参与计算） */
	public static final String MERGE_TYPE_WAVG2 = "wavg2";
	
	/**对象类型：01 组件，02 设备，03 device_group，04 区域，05 电站，06 部门，07 psr，08 型号，09 类型*/
	public static final String POINT_OBJ_TYPE_DEVICE="02";
	
	/**是否显示:0不显示，1显示，2扩展显示*/
	public static final String POINT_IS_DISPLAY_HIDE = "2";
	public static final String POINT_IS_DISPLAY_SHOW = "1";
	public static final String POINT_IS_DISPLAY_NONE = "0";
	
	/** 测点状态：0 正常，1 作废 */
	public static final String POINT_STATUS_NORMAL = "0";
	/** 测点状态：0 正常，1 作废 */
	public static final String POINT_STATUS_DELETED = "1";
	/** 测点类目状态：0 正常，1 作废 */
	public static final String CATE_STATUS_NORMAL = "0";
	/** 测点类目状态：0 正常，1 作废 */
	public static final String CATE_STATUS_DELETED = "1";
	
	public static final String CATE_ID_AM_DEVICE_ALARM_PREFIX = "AM-DEVICE-ALARM-";
	public static final String CATE_ID_AM_DEVICE_ALARM_FAULT = "AM-DEVICE-ALARM-FAULT";	//故障
	public static final String CATE_ID_AM_DEVICE_ALARM_WARN = "AM-DEVICE-ALARM-WARN";	//警告
	public static final String CATE_ID_AM_DEVICE_ALARM_INFO = "AM-DEVICE-ALARM-INFO";	//提示
	public static final String CATE_ID_AM_DEVICE_ALARM_LOG= "AM-DEVICE-ALARM-LOG";		//日志
	
	public static final String CATE_ID_AM_E2BOX_POWEROFF = "AM-E2BOX-POWEROFF-ALARM";	//电站断电告警

	/** 设备状态：0 正常，1 有告警，2 有缺陷 */
	public static final String CATE_ID_DEVICE_STS = "A-DEVICE-STATUS";
	public static final String CATE_ID_STATION_STS = "A-STATION-STATUS";
	/** 设备通讯状态：0 正常，1 通讯中断 */
	public static final String CATE_ID_COMM_STS = "A-DEVICE-COMMUNICATION-STATUS";
	/** 设备当前告警数 */
	public static final String CATE_ID_ALARM_NUM = "A-alarmUnfinishedAll-RT";
	/** 设备当前缺陷数 */
	public static final String CATE_ID_DEFECT_NUM = "A-defectUnfinishedAll-RT";

	public static final String CATE_ID_AI_DZ_GLDNB = "AI-DZ-GLDNB";	//实时总功率(电表)
	public static final String CATE_ID_AI_DZ_GLNB = "AI-DZ-GLNB";	//实时总功率(逆变器)
	public static final String CATE_ID_STATION_POWER = "A-POWER_RT";
	public static final String CATE_ID_STATION_MAXPOWER_D = "A-MAXPOWER_DAY";
	public static final String CATE_ID_STATION_MAXPOWERTIME_D = "A-MAXPOWERTIME_DAY";
	
	public static final String CATE_ID_STATION_ENERGY_ALL = "A-ENERGY-ALL";
	public static final String CATE_ID_STATION_ENERGY_HOUR = "A-ENERGY-HOUR";
	public static final String CATE_ID_STATION_ENERGY_DAY = "A-ENERGY-DAY";
	public static final String CATE_ID_STATION_ENERGY_MON = "A-ENERGY-MON";
	public static final String CATE_ID_STATION_ENERGY_YEAR = "A-ENERGY-YEAR";
	public static final String CATE_ID_AI_DZ_YGZDLDNB_A = "AI-DZ-YGZDLDNB-A ";	//累计发电量(电表)
	public static final String CATE_ID_AI_DZ_YGZDLDNB_D = "AI-DZ-YGZDLDNB-D ";	//每日发电量(电表)
	public static final String CATE_ID_AI_DZ_YGZDLDNB_H = "AI-DZ-YGZDLDNB-H ";	//每小时发电量(电表)
	public static final String CATE_ID_AI_DZ_YGZDLDNB_M = "AI-DZ-YGZDLDNB-M ";	//每月发电量(电表)
	public static final String CATE_ID_AI_DZ_YGZDLDNB_Y = "AI-DZ-YGZDLDNB-Y ";	//每年发电量(电表)
	public static final String CATE_ID_AI_DZ_YGZDLNB_A = "AI-DZ-YGZDLNB-A ";	//累计发电量(逆变器)
	public static final String CATE_ID_AI_DZ_YGZDLNB_D = "AI-DZ-YGZDLNB-D ";	//每日发电量(逆变器)
	public static final String CATE_ID_AI_DZ_YGZDLNB_H = "AI-DZ-YGZDLNB-H ";	//每小时发电量(逆变器)
	public static final String CATE_ID_AI_DZ_YGZDLNB_M = "AI-DZ-YGZDLNB-M ";	//每月发电量(逆变器)
	public static final String CATE_ID_AI_DZ_YGZDLNB_Y = "AI-DZ-YGZDLNB-Y ";	//每年发电量(逆变器)

	public static final String CATE_ID_AM_DNB_ZXYGZDN = "AM-DNB-ZXYGZDN";
	public static final String CATE_ID_AM_DNB_ZXYGZGL = "AM-DNB-ZXYGZGL";	//正向有功总功率
	public static final String CATE_ID_AM_DNB_FXYGZGL = "AM-DNB-FXYGZGL";	//反向有功总功率
	
	public static final String CATE_ID_AM_NB_POWER = "AM-NB-ZXYGGL-GRID";
	public static final String CATE_ID_AM_NB_ENERGY_ALL = "AM-NB-ZXYGDD-A";
	public static final String CATE_ID_AM_NB_ENERGY_DAY = "AM-NB-ZXYGDD-D";
	public static final String CATE_ID_AM_NB_PVI_INPUT = "AM-NB-PVI-INPUT";
	public static final String CATE_ID_AI_NB_MAXP_D = "AI-NB-MAXP-D";
	public static final String CATE_ID_AI_NB_MAXP_TIME_D = "AI-NB-MAXPTIME-D";
	public static final String CATE_ID_AI_NB_ZXYGDD_H = "AI-NB-ZXYGDD-H";
	public static final String CATE_ID_AI_NB_ZXYGDD_D = "AI-NB-ZXYGDD-D";
	public static final String CATE_ID_AI_NB_ZXYGDD_M = "AI-NB-ZXYGDD-M";
	public static final String CATE_ID_AI_NB_ZXYGDD_Y = "AI-NB-ZXYGDD-Y";
	public static final String CATE_ID_AI_NB_ZXYGDXS_H = "AI-NB-ZXYGDXS-H";	//小时发电等效时数
	public static final String CATE_ID_AI_NB_ZXYGDXS_YD = "AI-NB-ZXYGDXS-YD";	//昨日发电等效时数
	public static final String CATE_ID_AI_NB_ZXYGDXS_D = "AI-NB-ZXYGDXS-D";	//日发电等效时数
	public static final String CATE_ID_AI_NB_ZXYGDXS_M = "AI-NB-ZXYGDXS-M";	//月发电等效时数
	public static final String CATE_ID_AI_NB_ZXYGDXS_Y = "AI-NB-ZXYGDXS-Y";	//年发电等效时数

	public static final String CATE_ID_AM_HLX_PVI_INPUT = "AM-HLX-PVI-INPUT";
	public static final String CATE_ID_AM_HLX_PVU_INPUT = "AM-HLX-PVU-INPUT";

	public static final String CATE_ID_AI_DNB_ZXYGZDN_A = "AI-DNB-ZXYGZDN-A";
	public static final String CATE_ID_AI_DNB_ZXYGZDN_H = "AI-DNB-ZXYGZDN-H";
	public static final String CATE_ID_AI_DNB_ZXYGZDN_D = "AI-DNB-ZXYGZDN-D";
	public static final String CATE_ID_AI_DNB_ZXYGZDN_M = "AI-DNB-ZXYGZDN-M";
	public static final String CATE_ID_AI_DNB_ZXYGZDN_Y = "AI-DNB-ZXYGZDN-Y";

	public static final String CATE_ID_TH_TEMP = "AM-TH-TEMP";		//温湿度计-温度
	public static final String CATE_ID_TH_HYGRO = "AM-TH-HYGRO";	//温湿度计-湿度
	public static final String CATE_ID_PM25 = "AM-PM25";
	public static final String CATE_ID_PM10 = "AM-PM10";

	public static final String CATE_ID_DOOR_STATUS = "AM-DOOR-STATUS";	//门禁 : 关门 (0) 
	public static final String CATE_ID_DOOR_CARDNO = "AM-DOOR-CARDNO";	//刷卡卡号 : 
	public static final String CATE_ID_DOOR_ALARM = "AM-DOOR-ALARM";	//报警 : 无报警 (0) 
	public static final String CATE_ID_DOOR_EVENT = "AM-DOOR-EVENT";	//事件 : 正常通过 (0)
	
	public static final String CATE_ID_KT_TEMP_OUT = "AM-KT-TEMP-OUT";			//室温
	public static final String CATE_ID_KT_TEMP_SET = "AM-KT-TEMP-SET";			//温度设定
	public static final String CATE_ID_KT_RUNMODE = "AM-KT-RUNMODE";			//运行模式
	public static final String CATE_ID_KT_RUNMODE_SET = "AM-KT-RUNMODE-SET";	//运行模式设定
	public static final String CATE_ID_KT_STATUS = "AM-KT-STATUS";				//运行状态（空调开关）
	public static final String CATE_ID_KT_STATUS_SET = "AM-KT-RUNSTATUS";		//运行状态设定（空调开关）

	public static final String CATE_ID_AI_DZ_QXFZL_A = "AI-DZ-QXFZL-A";
	public static final String CATE_ID_AI_DZ_QXFZL_H = "AI-DZ-QXFZL-H";
	public static final String CATE_ID_AI_DZ_QXFZL_D = "AI-DZ-QXFZL-D";
	public static final String CATE_ID_AI_DZ_QXFZL_M = "AI-DZ-QXFZL-M";
	public static final String CATE_ID_AI_DZ_QXFZL_Y = "AI-DZ-QXFZL-Y";

	public static final String CATE_ID_AI_EBS_CHARGE_H = "AI-EBS-CHARGE-H";			// 堆小时充电电量
	public static final String CATE_ID_AI_EBS_DISCHARGE_H = "AI-EBS-DISCHARGE-H";	// 堆小时放电电量
	public static final String CATE_ID_AI_EBS_CHARGE_D = "AI-EBS-CHARGE-D";			// 堆日充电电量
	public static final String CATE_ID_AI_EBS_DISCHARGE_D = "AI-EBS-DISCHARGE-D";	// 堆日放电电量
	public static final String CATE_ID_AM_EBS_CHARGE_A = "AM-EBS-CHARGE-A";			// 堆累计充电电量
	public static final String CATE_ID_AM_EBS_DISCHARGE_A = "AM-EBS-DISCHARGE-A";	// 堆累计放电电量

	/* 数据存储周期 */
	/**不存储*/
	public static final String POINT_DATA_PERIOD_NOTSAVE = "00";
	/**变位存储*/
	public static final String POINT_DATA_PERIOD_ONCHANGE = "01";
	/**1分钟*/
	public static final String POINT_DATA_PERIOD_1MIN = "02";
	/**5分钟*/
	public static final String POINT_DATA_PERIOD_5MIN = "03";
	/**10分钟*/
	public static final String POINT_DATA_PERIOD_10MIN = "04";
	/**15分钟*/
	public static final String POINT_DATA_PERIOD_15MIN = "05";
	/**每小时*/
	public static final String POINT_DATA_PERIOD_HOUR = "10";
	/**每天*/
	public static final String POINT_DATA_PERIOD_DAY = "20";
	/**每月*/
	public static final String POINT_DATA_PERIOD_MONTH = "30";
	/**每年*/
	public static final String POINT_DATA_PERIOD_YEAR = "40";
	
	/* 测点计算周期 */
	/**实时*/
	public static final String POINT_CALC_PERIOD_REALTIME = "01";
	/**1分钟*/
	public static final String POINT_CALC_PERIOD_1MIN = "02";
	/**5分钟*/
	public static final String POINT_CALC_PERIOD_5MIN = "03";
	/**10分钟*/
	public static final String POINT_CALC_PERIOD_10MIN = "04";
	/**15分钟*/
	public static final String POINT_CALC_PERIOD_15MIN = "05";
	/**每小时*/
	public static final String POINT_CALC_PERIOD_HOUR = "10";
	/**每天*/
	public static final String POINT_CALC_PERIOD_DAY = "20";
	/**每月*/
	public static final String POINT_CALC_PERIOD_MONTH = "30";
	/**每年*/
	public static final String POINT_CALC_PERIOD_YEAR = "40";

	/* 四遥类型 */
	/**遥信*/
	public static final String POINT_REMOTION_TYPE_YX = "01";
	/**遥控*/
	public static final String POINT_REMOTION_TYPE_YK = "02";
	/**遥测*/
	public static final String POINT_REMOTION_TYPE_YC = "03";
	/**遥脉*/
	public static final String POINT_REMOTION_TYPE_YM = "04";
	
	/* 数据类型 */
	/** boolean */
	public static final String POINT_DATA_TYPE_BOOL = "01";
	public static final int POINT_VALUE_TYPE_BOOL = 1;
	/** long */
	public static final String POINT_DATA_TYPE_INT = "02";
	public static final int POINT_VALUE_TYPE_INT = 2;
	/** double */
	public static final String POINT_DATA_TYPE_DOUBLE = "03";
	public static final int POINT_VALUE_TYPE_DOUBLE = 3;
	/** string */
	public static final String POINT_DATA_TYPE_STRING = "04";
	public static final int POINT_VALUE_TYPE_STRING = 4;
	
	//数据存储时间精度
	/** 01 不截断 */
	public static final String POINT_PERSISTENCE_TYPE_RT = "01";
	/** 02 按分钟截断，例如 2017-09-14 15:22:37:899 -> 2017-09-14 15:22:00:000 */
	public static final String POINT_PERSISTENCE_TYPE_MIN = "02";
	/** 03 按小时截断，例如 2017-09-14 15:22:37:899 -> 2017-09-14 15:00:00:000 */
	public static final String POINT_PERSISTENCE_TYPE_HOUR = "03";
	/** 04 按日截断，例如 2017-09-14 15:22:37:899 -> 2017-09-14 00:00:00:000 */
	public static final String POINT_PERSISTENCE_TYPE_DAY = "04";
	/** 05 按月截断，例如 2017-09-14 15:22:37:899 -> 2017-09-01 00:00:00:000 */
	public static final String POINT_PERSISTENCE_TYPE_MON = "05";
	/** 06 按年截断，例如 2017-09-14 15:22:37:899 -> 2017-01-01 00:00:00:000 */
	public static final String POINT_PERSISTENCE_TYPE_YEAR = "06";

	public static final String CLASS_POINT_CATEGORY_RELA_TYPE_TOPO = "01";	//图元默认展示测点类目
	public static final String CLASS_POINT_CATEGORY_RELA_TYPE_DEFAULT = "02";	//对象标准测点关系
	
	/** 正常消息插入 */
	public static final int POINT_VALUE_SOURCE_CALC = 0;
	/** 老系统导入 */
	public static final int POINT_VALUE_SOURCE_OLD_SYSTEM = 1000;
	/** 每日凌晨自动生成的遥信遥控量 */
	public static final int POINT_VALUE_SOURCE_AUTO_CREATE = 2000;
	/** 由于数据质量问题，程序自动差补 */
	public static final int POINT_VALUE_SOURCE_AUTO_REPAIR = 3000;
	/** 由于数据质量问题，人工差补 */
	public static final int POINT_VALUE_SOURCE_MANUAL_REPAIR = 4000;
	/** 人工录入 */
	public static final int POINT_VALUE_SOURCE_MANUAL_INPUT = 5000;
	/** 重算 */
	public static final int POINT_VALUE_SOURCE_RECALC = 6000;

	/** 数据资料标识：有效数据 */
	public static final int POINT_VALUE_QUALITY_VALID = 0;
	/** 数据资料标识：300 过时数据 */
	public static final int POINT_VALUE_OUTTIME = 300;
	/** 数据资料标识：200 NaN */
	public static final int POINT_VALUE_QUALITY_NAN = 200;
	/** 数据资料标识：201 infinity */
	public static final int POINT_VALUE_QUALITY_INFINITY = 201;
	/** 数据资料标识：101 死数 */
	public static final int POINT_VALUE_QUALITY_DEAD = 101;
	/** 数据资料标识：102 超出合法值范围 */
	public static final int POINT_VALUE_QUALITY_INVALID_RANGE = 102;
	/** 数据资料标识：103 超出合理变化量范围 */
	public static final int POINT_VALUE_QUALITY_INVALID_CHANGE = 103;
	/** 数据资料标识：104 累积量计算非法 */
	public static final int POINT_VALUE_QUALITY_INVALID_CUMULATE = 104;
	/** 数据资料标识：105 超出合法时间范围 */
	public static final int POINT_VALUE_QUALITY_INVALID_TIME = 105;
	
	/** NEMS_CACHE_MEASURE_POINT缓存中queue的选项，用于判断是否向pushServer推送 */
	public static final String CACHE_QUEUE_WEBSOCKET = "websocket";
	/** NEMS_CACHE_MEASURE_POINT缓存中queue的选项，用于判断是否入库 */
	public static final String CACHE_QUEUE_SAVE2DB = "save2DB";
	/** NEMS_CACHE_MEASURE_POINT缓存中queue的选项，用于判断是否推送到告警程序 */
	public static final String CACHE_QUEUE_ALARM = "alarm";

	/** 告警规则 */
	public static final String BUS_TYPE_ALARM_RULE = "alarmRule";
	/** 测点预处理规则 */
	public static final String BUS_TYPE_PREPROCESS = "pointpreprocess";

	public static final String COLLECTION_NAME_YEAR = "nems_values_year";
	public static final String COLLECTION_NAME_MON = "nems_values_month";
	public static final String COLLECTION_NAME_DAY = "nems_values_day";
	public static final String COLLECTION_NAME_HOUR_PREFIX = "nems_values_hour_";
	public static final String COLLECTION_NAME_REAL_PREFIX = "nems_values_";

	public static final String TABLE_NAME_YEAR = "point_data_year";
	public static final String TABLE_NAME_MON = "point_data_month";
	public static final String TABLE_NAME_DAY = "point_data_day";
	public static final String TABLE_NAME_HOUR = "point_data_hour";
	public static final String TABLE_NAME_REAL = "point_data_real";

	//标准测点映射方式
	public static final String POINT_CLASS_REPLACE = "replace";		//替换
	public static final String POINT_CLASS_MAP = "mapSysCateId";	//映射

	public static List<String> DISPERSION_DEVICE_CATEIDS = Arrays.asList(new String[] {
		"AI-NB-DDRATIO-D", "AI-NB-DDRATIO-H", "AI-NB-DDRATIO-M", "AI-NB-DDRATIO-Y", 
		"AI-NB-ZXYGDD-D", "AI-NB-ZXYGDD-H", "AI-NB-ZXYGDD-M", "AI-NB-ZXYGDD-Y", 
		"AI-NB-ZXYGDXS-D", "AI-NB-ZXYGDXS-H", "AI-NB-ZXYGDXS-M", "AI-NB-ZXYGDXS-Y",
		"AI-ZC-DWPVI-D", "AI-ZC-DWPVI-H", "AI-ZC-DWPVI-M", "AI-ZC-DWPVI-Y", 
		"AI-ZC-PVI-D", "AI-ZC-PVI-H", "AI-ZC-PVI-M", "AI-ZC-PVI-Y"
	});

}
