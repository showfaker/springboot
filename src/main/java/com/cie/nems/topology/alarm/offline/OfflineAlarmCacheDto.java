package com.cie.nems.topology.alarm.offline;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.topology.cache.point.value.PointValueDto;

public class OfflineAlarmCacheDto {
	private Date startRunTime;
	private int startHHmm;		//通讯状态处理开始时间
	private int endHHmm;		//通讯状态处理停止时间
	private int startAlarmHHmm;		//告警处理开始时间
	private int endAlarmHHmm;		//告警处理停止时间
	private long offlineDuration;
	private long outlineDuration;
	private Boolean filterByOutlineAndParent;
	
	Map<String, List<Device>> outlines = new HashMap<String, List<Device>>();
	Map<String, List<Device>> otherDevices = new HashMap<String, List<Device>>();
	Map<String, PointValueDto> deviceCommuValues = new HashMap<String, PointValueDto>();
	
	public Date getStartRunTime() {
		return startRunTime;
	}
	public void setStartRunTime(Date startRunTime) {
		this.startRunTime = startRunTime;
	}
	public int getStartHHmm() {
		return startHHmm;
	}
	public void setStartHHmm(int startHHmm) {
		this.startHHmm = startHHmm;
	}
	public int getEndHHmm() {
		return endHHmm;
	}
	public void setEndHHmm(int endHHmm) {
		this.endHHmm = endHHmm;
	}
	public long getOfflineDuration() {
		return offlineDuration;
	}
	public void setOfflineDuration(long offlineDuration) {
		this.offlineDuration = offlineDuration;
	}
	public long getOutlineDuration() {
		return outlineDuration;
	}
	public void setOutlineDuration(long outlineDuration) {
		this.outlineDuration = outlineDuration;
	}
	public Map<String, List<Device>> getOutlines() {
		return outlines;
	}
	public void setOutlines(Map<String, List<Device>> outlines) {
		this.outlines = outlines;
	}
	public Map<String, List<Device>> getOtherDevices() {
		return otherDevices;
	}
	public void setOtherDevices(Map<String, List<Device>> otherDevices) {
		this.otherDevices = otherDevices;
	}
	public Boolean getFilterByOutlineAndParent() {
		return filterByOutlineAndParent;
	}
	public void setFilterByOutlineAndParent(Boolean filterByOutlineAndParent) {
		this.filterByOutlineAndParent = filterByOutlineAndParent;
	}
	public Map<String, PointValueDto> getDeviceCommuValues() {
		return deviceCommuValues;
	}
	public void setDeviceCommuValues(Map<String, PointValueDto> deviceCommuValues) {
		this.deviceCommuValues = deviceCommuValues;
	}
	public int getStartAlarmHHmm() {
		return startAlarmHHmm;
	}
	public void setStartAlarmHHmm(int startAlarmHHmm) {
		this.startAlarmHHmm = startAlarmHHmm;
	}
	public int getEndAlarmHHmm() {
		return endAlarmHHmm;
	}
	public void setEndAlarmHHmm(int endAlarmHHmm) {
		this.endAlarmHHmm = endAlarmHHmm;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
