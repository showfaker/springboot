package com.cie.nems.alarm.rule;

import java.util.Date;
import java.util.List;

import com.cie.nems.common.service.CommonService;


public class AlarmRule {
	private String stationId;
	private Long ruleId;
	private Long parentId;
	private String ruleType;
	private String alarmSource;
	private String alarmLevel;
	private String alarmType;
	private String alarmText;
	private String compareSymbol;
	private String compareVal;
	private List<String> compareVals;
	private Double doubleCompareVal;
	private List<Double> doubleCompareVals;
	private Integer duration;
	private Boolean hasSideCond;
	private String alarmCheckType;
	private Date beginDate;
	private Date endDate;
	private Integer beginTime;
	private Integer endTime;
	
	private List<AlarmSideCond> sideConds;
	
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public Long getRuleId() {
		return ruleId;
	}
	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
	public Long getParentId() {
		return parentId;
	}
	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}
	public String getRuleType() {
		return ruleType;
	}
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}
	public String getAlarmSource() {
		return alarmSource;
	}
	public void setAlarmSource(String alarmSource) {
		this.alarmSource = alarmSource;
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
	public String getAlarmText() {
		return alarmText;
	}
	public void setAlarmText(String alarmText) {
		this.alarmText = alarmText;
	}
	public String getCompareSymbol() {
		return compareSymbol;
	}
	public void setCompareSymbol(String compareSymbol) {
		this.compareSymbol = compareSymbol;
	}
	public String getCompareVal() {
		return compareVal;
	}
	public void setCompareVal(String compareVal) {
		this.compareVal = compareVal;
	}
	public List<String> getCompareVals() {
		return compareVals;
	}
	public void setCompareVals(List<String> compareVals) {
		this.compareVals = compareVals;
	}
	public List<Double> getDoubleCompareVals() {
		return doubleCompareVals;
	}
	public void setDoubleCompareVals(List<Double> doubleCompareVals) {
		this.doubleCompareVals = doubleCompareVals;
	}
	public Double getDoubleCompareVal() {
		return doubleCompareVal;
	}
	public void setDoubleCompareVal(Double doubleCompareVal) {
		this.doubleCompareVal = doubleCompareVal;
	}
	public Integer getDuration() {
		return duration;
	}
	public void setDuration(Integer duration) {
		this.duration = duration;
	}
	public Boolean getHasSideCond() {
		return hasSideCond;
	}
	public void setHasSideCond(Boolean hasSideCond) {
		this.hasSideCond = hasSideCond;
	}
	public String getAlarmCheckType() {
		return alarmCheckType;
	}
	public void setAlarmCheckType(String alarmCheckType) {
		this.alarmCheckType = alarmCheckType;
	}
	public Date getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(Date beginDate) {
		this.beginDate = beginDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
	public Integer getBeginTime() {
		return beginTime;
	}
	public void setBeginTime(Integer beginTime) {
		this.beginTime = beginTime;
	}
	public Integer getEndTime() {
		return endTime;
	}
	public void setEndTime(Integer endTime) {
		this.endTime = endTime;
	}
	public List<AlarmSideCond> getSideConds() {
		return sideConds;
	}
	public void setSideConds(List<AlarmSideCond> sideConds) {
		this.sideConds = sideConds;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}