package com.cie.nems.topology.alarm;

import com.cie.nems.common.service.CommonService;

public class AlarmMsg {
	private String alarmMsgType;
	private Long ruleId;
	private String psrId;
	private String stationId;
	private String v;
	private Long dt;
	private String alarmText;
	public String getAlarmMsgType() {
		return alarmMsgType;
	}
	public void setAlarmMsgType(String alarmMsgType) {
		this.alarmMsgType = alarmMsgType;
	}
	public Long getRuleId() {
		return ruleId;
	}
	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
	public String getPsrId() {
		return psrId;
	}
	public void setPsrId(String psrId) {
		this.psrId = psrId;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	public Long getDt() {
		return dt;
	}
	public void setDt(Long dt) {
		this.dt = dt;
	}
	public String getAlarmText() {
		return alarmText;
	}
	public void setAlarmText(String alarmText) {
		this.alarmText = alarmText;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
