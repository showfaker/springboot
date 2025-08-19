package com.cie.nems.device;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DeviceService {
	/** 未知 */
	public static final String DEVICE_TYPE_UNKNOWN = "0";
	/** 虚拟的合并组串，绘制拓扑图时使用 */
	public static final String DEVICE_TYPE_VZC = "VISUAL_ZC";
	/** 组串 */
	public static final String DEVICE_TYPE_ZC = "ZC";
	/** 组件 */
	public static final String DEVICE_TYPE_ZJ = "ZJ";
	/** 直流汇流箱 */
	public static final String DEVICE_TYPE_HLX = "HLX";
	/** 交流汇流箱 */
	public static final String DEVICE_TYPE_JHL = "JHL";
	/** 分布式逆变器 */
	public static final String DEVICE_TYPE_NB = "NB";
	/** 集中式逆变器 */
	public static final String DEVICE_TYPE_JNB = "JNB";
	/** 变压器 */
	public static final String DEVICE_TYPE_BYQ = "BYQ";
	/** 高压柜 */
	public static final String DEVICE_TYPE_GYKG = "GYKG";
	/** 并网点 */
	public static final String DEVICE_TYPE_BWD = "BWD";
	/** 电能表 */
	public static final String DEVICE_TYPE_DNB = "DNB";
	/** 气象站 */
	public static final String DEVICE_TYPE_QXZ = "QXZ";
	/** 其他 */
	public static final String DEVICE_TYPE_QT = "QT";
	/** 不采集类设备 */
	public static final String DEVICE_TYPE_BCJ = "BCJ";
	/** E2Box */
	public static final String DEVICE_TYPE_E2BOX = "E2BOX";

	/** 外线 */
	public static final String USE_FLAG_OUTLINE = "01";
	/** 集电线路 */
	public static final String USE_FLAG_COLLECT_LINE = "02";
	/** 总发电量表 */
	public static final String USE_FLAG_TOTAL_METER = "10";

	//设备运行状态
	public static final String DEVICE_RUN_STATUS_RUNNING = "1"; // 运行
	public static final String DEVICE_RUN_STATUS_STOP = "0"; // 停机
	
	public Page<String> getPsrIds(List<Integer> channelIds, Pageable pageable);
	
	public Page<Device> getDevices(List<Integer> channelIds, List<String> stationIds, List<String> deviceIds, 
			Pageable pageable);

}
