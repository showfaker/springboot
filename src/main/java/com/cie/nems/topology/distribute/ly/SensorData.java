package com.cie.nems.topology.distribute.ly;

import java.io.Serializable;

import com.cie.nems.common.service.CommonService;

public class SensorData implements Serializable {
	private static final long serialVersionUID = 1L;

	/** 数据时间 */
	private Long dt;
	/** 测点值 */
	private String v;
	/** 测点编号 */
	private Long pid;
	/** 设备ID */
	private String deviceId;
	
	public Long getDt() {
		return dt;
	}
	public void setDt(Long dt) {
		this.dt = dt;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
