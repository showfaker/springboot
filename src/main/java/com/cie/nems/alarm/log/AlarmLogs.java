package com.cie.nems.alarm.log;

import java.io.Serializable;
import javax.persistence.*;

import com.cie.nems.common.service.CommonService;

import java.util.Date;


/**
 * The persistent class for the alarm_log database table.
 * 
 */
@Entity
@Table(name="alarm_log")
@NamedQuery(name="AlarmLog.findAll", query="SELECT a FROM AlarmLogs a")
public class AlarmLogs implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="log_id")
	private String logId;

	@Column(name="affect_bus")
	private String affectBus;

	@Column(name="affect_per")
	private String affectPer;

	@Column(name="alarm_action")
	private String alarmAction;

	@Column(name="alarm_check_memo")
	private String alarmCheckMemo;

	@Column(name="alarm_check_status")
	private String alarmCheckStatus;

	@Column(name="alarm_check_time")
	private Date alarmCheckTime;

	@Column(name="alarm_checker")
	private String alarmChecker;

	@Column(name="alarm_level")
	private String alarmLevel;

	@Column(name="alarm_pic")
	private String alarmPic;

	@Column(name="alarm_source")
	private String alarmSource;

	@Column(name="alarm_status")
	private String alarmStatus;

	@Column(name="alarm_text")
	private String alarmText;

	@Column(name="alarm_type")
	private String alarmType;

	@Column(name="area_id")
	private String areaId;

	@Column(name="comp_id")
	private String compId;

	@Column(name="defect_id")
	private Long defectId;

	@Column(name="device_id")
	private String deviceId;

	@Column(name="end_time")
	private Date endTime;

	@Column(name="point_id")
	private Long pointId;

	@Column(name="psr_id")
	private String psrId;

	@Column(name="rela_id")
	private String relaId;

	@Column(name="rule_id")
	private Long ruleId;

	@Column(name="start_time")
	private Date startTime;

	@Column(name="partition_id")
	private Integer partitionId;

	@Column(name="station_id")
	private String stationId;

	@Column(name="create_time")
	private Date createTime;

	@Column(name="update_time")
	private Date updateTime;
	
	public String getLogId() {
		return logId;
	}
	public void setLogId(String logId) {
		this.logId = logId;
	}
	public String getAffectBus() {
		return affectBus;
	}
	public void setAffectBus(String affectBus) {
		this.affectBus = affectBus;
	}
	public String getAffectPer() {
		return affectPer;
	}
	public void setAffectPer(String affectPer) {
		this.affectPer = affectPer;
	}
	public String getAlarmAction() {
		return alarmAction;
	}
	public void setAlarmAction(String alarmAction) {
		this.alarmAction = alarmAction;
	}
	public String getAlarmCheckMemo() {
		return alarmCheckMemo;
	}
	public void setAlarmCheckMemo(String alarmCheckMemo) {
		this.alarmCheckMemo = alarmCheckMemo;
	}
	public String getAlarmCheckStatus() {
		return alarmCheckStatus;
	}
	public void setAlarmCheckStatus(String alarmCheckStatus) {
		this.alarmCheckStatus = alarmCheckStatus;
	}
	public Date getAlarmCheckTime() {
		return alarmCheckTime;
	}
	public void setAlarmCheckTime(Date alarmCheckTime) {
		this.alarmCheckTime = alarmCheckTime;
	}
	public String getAlarmChecker() {
		return alarmChecker;
	}
	public void setAlarmChecker(String alarmChecker) {
		this.alarmChecker = alarmChecker;
	}
	public String getAlarmLevel() {
		return alarmLevel;
	}
	public void setAlarmLevel(String alarmLevel) {
		this.alarmLevel = alarmLevel;
	}
	public String getAlarmPic() {
		return alarmPic;
	}
	public void setAlarmPic(String alarmPic) {
		this.alarmPic = alarmPic;
	}
	public String getAlarmSource() {
		return alarmSource;
	}
	public void setAlarmSource(String alarmSource) {
		this.alarmSource = alarmSource;
	}
	public String getAlarmStatus() {
		return alarmStatus;
	}
	public void setAlarmStatus(String alarmStatus) {
		this.alarmStatus = alarmStatus;
	}
	public String getAlarmText() {
		return alarmText;
	}
	public void setAlarmText(String alarmText) {
		this.alarmText = alarmText;
	}
	public String getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(String alarmType) {
		this.alarmType = alarmType;
	}
	public String getAreaId() {
		return areaId;
	}
	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}
	public String getCompId() {
		return compId;
	}
	public void setCompId(String compId) {
		this.compId = compId;
	}
	public Long getDefectId() {
		return defectId;
	}
	public void setDefectId(Long defectId) {
		this.defectId = defectId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public Long getPointId() {
		return pointId;
	}
	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}
	public String getPsrId() {
		return psrId;
	}
	public void setPsrId(String psrId) {
		this.psrId = psrId;
	}
	public String getRelaId() {
		return relaId;
	}
	public void setRelaId(String relaId) {
		this.relaId = relaId;
	}
	public Long getRuleId() {
		return ruleId;
	}
	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Integer getPartitionId() {
		return partitionId;
	}
	public void setPartitionId(Integer partitionId) {
		this.partitionId = partitionId;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}