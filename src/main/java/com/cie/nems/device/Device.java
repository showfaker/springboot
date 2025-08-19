package com.cie.nems.device;

import com.cie.nems.common.service.CommonService;

public class Device {
	private String deviceId;
	private String deviceName;
	private String psrId;
	private String parentId;
	private String areaId;
	private String stationId;
	private String customerId;
	private String deviceType;
	private String useFlag;
	private Double capacity;
	private String commuStatus;
	private String preCommuStatus;
	private Long commuPointId;
	private Integer calcChannel;
	
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public String getPsrId() {
		return psrId;
	}
	public void setPsrId(String psrId) {
		this.psrId = psrId;
	}
	public String getAreaId() {
		return areaId;
	}
	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}
	public Double getCapacity() {
		return capacity;
	}
	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	public String getCommuStatus() {
		return commuStatus;
	}
	public void setCommuStatus(String commuStatus) {
		this.commuStatus = commuStatus;
	}
	public String getParentId() {
		return parentId;
	}
	public void setParentId(String parentId) {
		this.parentId = parentId;
	}
	public String getUseFlag() {
		return useFlag;
	}
	public void setUseFlag(String useFlag) {
		this.useFlag = useFlag;
	}
	public Integer getCalcChannel() {
		return calcChannel;
	}
	public void setCalcChannel(Integer calcChannel) {
		this.calcChannel = calcChannel;
	}
	public String getPreCommuStatus() {
		return preCommuStatus;
	}
	public void setPreCommuStatus(String preCommuStatus) {
		this.preCommuStatus = preCommuStatus;
	}
	public Long getCommuPointId() {
		return commuPointId;
	}
	public void setCommuPointId(Long commuPointId) {
		this.commuPointId = commuPointId;
	}
	
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
