package com.cie.nems.topology.alarm;

import java.util.Date;

import com.cie.nems.common.service.CommonService;

public class AlarmNoticeDto {
	private String logId;
	private String customerId;
	private String stationId;
	private Long groupId;
	private String deviceId;
	private String alarmLevel;
	private String alarmType;
	private String alarmAction;
	private String alarmText;
	private Date startTime;
	private Date endTime;
	private String stationName;
	private String groupName;
	private String deviceName;
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getAlarmLevel() {
		return alarmLevel;
	}
	public void setAlarmLevel(String alarmLevel) {
		this.alarmLevel = alarmLevel;
	}
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	public String getAlarmAction() {
		return alarmAction;
	}
	public void setAlarmAction(String alarmAction) {
		this.alarmAction = alarmAction;
	}
	public String getAlarmText() {
		return alarmText;
	}
	public void setAlarmText(String alarmText) {
		this.alarmText = alarmText;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public String getStationName() {
		return stationName;
	}
	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	public String getDeviceName() {
		return deviceName;
	}
	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
