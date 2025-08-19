package com.cie.nems.topology.alarm.offline;

import java.io.Serializable;
import javax.persistence.*;

import com.cie.nems.alarm.log.AlarmLogs;
import com.cie.nems.common.service.CommonService;

import java.util.Date;
import java.util.ArrayList;
import java.util.List;


/**
 * The persistent class for the device_commu_status database table.
 * 
 */
@Entity
@Table(name="device_commu_status")
@NamedQuery(name="DeviceCommuStatus.findAll", query="SELECT d FROM DeviceCommuStatus d")
public class DeviceCommuStatus implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="status_id")
	private String statusId;

	@Column(name="area_id")
	private String areaId;

	@Column(name="batch_no")
	private String batchNo;

	@Column(name="class_id")
	private String classId;

	@Column(name="commu_point_value")
	private Integer commuPointValue;

	@Column(name="create_time")
	private Date createTime;

	@Column(name="device_id")
	private String deviceId;

	@Column(name="device_type")
	private String deviceType;

	@Column(name="station_id")
	private String stationId;

	@Column(name="yc_dead_points")
	private Integer ycDeadPoints = 0;

	@Column(name="yc_hasvalue_points")
	private Integer ycHasvaluePoints = 0;

	@Column(name="yc_max_dt")
	private Date ycMaxDt = null;

	@Column(name="yc_min_dt")
	private Date ycMinDt = null;

	@Column(name="yc_novalue_points")
	private Integer ycNovaluePoints = 0;

	@Column(name="yc_undead_points")
	private Integer ycUndeadPoints = 0;

	@Column(name="yx_dead_points")
	private Integer yxDeadPoints = 0;

	@Column(name="yx_hasvalue_points")
	private Integer yxHasvaluePoints = 0;

	@Column(name="yx_max_dt")
	private Date yxMaxDt = null;

	@Column(name="yx_min_dt")
	private Date yxMinDt = null;

	@Column(name="yx_novalue_points")
	private Integer yxNovaluePoints = 0;

	@Column(name="yx_undead_points")
	private Integer yxUndeadPoints = 0;

	@Column(name="alarm_action")
	private String alarmAction;

	@Column(name="log_id")
	private String logId;

	@Column(name="alarm_reason")
	private String alarmReason;

	@Transient
	private AlarmLogs alarmStatus;
	
	@Transient
	private Long commuPointId;
	
	@Transient
	private List<Integer> commuStatus;

	@Transient
	private Integer outlineCommuValue;

	public DeviceCommuStatus() {
	}

	public DeviceCommuStatus(String statusId, String areaId, String batchNo, String classId, 
			Integer commuPointValue, Date createTime, String deviceId, String deviceType, String stationId, 
			Integer ycDeadPoints, Integer ycHasvaluePoints, Date ycMaxDt, Date ycMinDt, 
			Integer ycNovaluePoints, Integer ycUndeadPoints, Integer yxDeadPoints, Integer yxHasvaluePoints, 
			Date yxMaxDt, Date yxMinDt, Integer yxNovaluePoints, Integer yxUndeadPoints, String alarmAction, 
			String logId, String alarmReason, AlarmLogs alarmStatus, Long commuPointId, 
			List<Integer> commuStatus, Integer outlineCommuValue) {
		super();
		this.statusId = statusId;
		this.areaId = areaId;
		this.batchNo = batchNo;
		this.classId = classId;
		this.commuPointValue = commuPointValue;
		this.createTime = createTime;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.stationId = stationId;
		this.ycDeadPoints = ycDeadPoints;
		this.ycHasvaluePoints = ycHasvaluePoints;
		this.ycMaxDt = ycMaxDt;
		this.ycMinDt = ycMinDt;
		this.ycNovaluePoints = ycNovaluePoints;
		this.ycUndeadPoints = ycUndeadPoints;
		this.yxDeadPoints = yxDeadPoints;
		this.yxHasvaluePoints = yxHasvaluePoints;
		this.yxMaxDt = yxMaxDt;
		this.yxMinDt = yxMinDt;
		this.yxNovaluePoints = yxNovaluePoints;
		this.yxUndeadPoints = yxUndeadPoints;
		this.alarmAction = alarmAction;
		this.logId = logId;
		this.alarmReason = alarmReason;
		this.alarmStatus = alarmStatus;
		this.commuPointId = commuPointId;
		this.commuStatus = commuStatus;
		this.outlineCommuValue = outlineCommuValue;
	}

	public String getStatusId() {
		return this.statusId;
	}

	public void setStatusId(String statusId) {
		this.statusId = statusId;
	}

	public String getAreaId() {
		return this.areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getBatchNo() {
		return this.batchNo;
	}

	public void setBatchNo(String batchNo) {
		this.batchNo = batchNo;
	}

	public String getClassId() {
		return this.classId;
	}

	public void setClassId(String classId) {
		this.classId = classId;
	}

	public Integer getCommuPointValue() {
		return this.commuPointValue;
	}

	public void setCommuPointValue(Integer commuPointValue) {
		this.commuPointValue = commuPointValue;
	}

	public Date getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public String getStationId() {
		return this.stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public Integer getYcDeadPoints() {
		return this.ycDeadPoints;
	}

	public void setYcDeadPoints(Integer ycDeadPoints) {
		this.ycDeadPoints = ycDeadPoints;
	}

	public Integer getYcHasvaluePoints() {
		return this.ycHasvaluePoints;
	}

	public void setYcHasvaluePoints(Integer ycHasvaluePoints) {
		this.ycHasvaluePoints = ycHasvaluePoints;
	}

	public Date getYcMaxDt() {
		return this.ycMaxDt;
	}

	public void setYcMaxDt(Date ycMaxDt) {
		this.ycMaxDt = ycMaxDt;
	}

	public void updateYcMaxDt(Date dt) {
		if (dt == null) return;
		if (this.ycMaxDt == null || dt.after(this.ycMaxDt)) {
			this.ycMaxDt = dt;
		}
	}

	public Date getYcMinDt() {
		return this.ycMinDt;
	}

	public void setYcMinDt(Date ycMinDt) {
		this.ycMinDt = ycMinDt;
	}

	public void updateYcMinDt(Date dt) {
		if (dt == null) return;
		if (this.ycMinDt == null || dt.before(this.ycMinDt)) {
			this.ycMinDt = dt;
		}
	}

	public Integer getYcNovaluePoints() {
		return this.ycNovaluePoints;
	}

	public void setYcNovaluePoints(Integer ycNovaluePoints) {
		this.ycNovaluePoints = ycNovaluePoints;
	}

	public Integer getYcUndeadPoints() {
		return this.ycUndeadPoints;
	}

	public void setYcUndeadPoints(Integer ycUndeadPoints) {
		this.ycUndeadPoints = ycUndeadPoints;
	}

	public Integer getYxDeadPoints() {
		return this.yxDeadPoints;
	}

	public void setYxDeadPoints(Integer yxDeadPoints) {
		this.yxDeadPoints = yxDeadPoints;
	}

	public Integer getYxHasvaluePoints() {
		return this.yxHasvaluePoints;
	}

	public void setYxHasvaluePoints(Integer yxHasvaluePoints) {
		this.yxHasvaluePoints = yxHasvaluePoints;
	}

	public Date getYxMaxDt() {
		return this.yxMaxDt;
	}

	public void setYxMaxDt(Date yxMaxDt) {
		this.yxMaxDt = yxMaxDt;
	}

	public void updateYxMaxDt(Date dt) {
		if (dt == null) return;
		if (this.yxMaxDt == null || dt.after(this.yxMaxDt)) {
			this.yxMaxDt = dt;
		}
	}

	public Date getYxMinDt() {
		return this.yxMinDt;
	}

	public void setYxMinDt(Date yxMinDt) {
		this.yxMinDt = yxMinDt;
	}

	public void updateYxMinDt(Date dt) {
		if (dt == null) return;
		if (this.yxMinDt == null || dt.before(this.yxMinDt)) {
			this.yxMinDt = dt;
		}
	}

	public Integer getYxNovaluePoints() {
		return this.yxNovaluePoints;
	}

	public void setYxNovaluePoints(Integer yxNovaluePoints) {
		this.yxNovaluePoints = yxNovaluePoints;
	}

	public Integer getYxUndeadPoints() {
		return this.yxUndeadPoints;
	}

	public void setYxUndeadPoints(Integer yxUndeadPoints) {
		this.yxUndeadPoints = yxUndeadPoints;
	}

	public Long getCommuPointId() {
		return commuPointId;
	}

	public void setCommuPointId(Long commuPointId) {
		this.commuPointId = commuPointId;
	}

	public List<Integer> getCommuStatus() {
		return commuStatus;
	}

	public void setCommuStatus(List<Integer> commuStatus) {
		this.commuStatus = commuStatus;
	}

	public String getAlarmAction() {
		return alarmAction;
	}

	public void setAlarmAction(String alarmAction) {
		this.alarmAction = alarmAction;
	}

	public String getLogId() {
		return logId;
	}

	public void setLogId(String logId) {
		this.logId = logId;
	}

	public String getAlarmReason() {
		return alarmReason;
	}

	public void setAlarmReason(String alarmReason) {
		this.alarmReason = alarmReason;
	}

	public Integer getOutlineCommuValue() {
		return outlineCommuValue;
	}

	public void setOutlineCommuValue(Integer outlineCommuValue) {
		this.outlineCommuValue = outlineCommuValue;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

	public Integer addYxHasvaluePoints() {
		return ++this.yxHasvaluePoints;
	}
	public Integer addYxNovaluePoints() {
		return ++this.yxNovaluePoints;
	}
	public Integer addYxDeadPoints() {
		return ++this.yxDeadPoints;
	}
	public Integer addYxUndeadPoints() {
		return ++this.yxUndeadPoints;
	}
	public Integer addYcHasvaluePoints() {
		return ++this.ycHasvaluePoints;
	}
	public Integer addYcNovaluePoints() {
		return ++this.ycNovaluePoints;
	}
	public Integer addYcDeadPoints() {
		return ++this.ycDeadPoints;
	}
	public Integer addYcUndeadPoints() {
		return ++this.ycUndeadPoints;
	}
	public void addCommuStatus(Double v) {
		if (v == null) return;
		if (this.commuStatus == null) {
			this.commuStatus = new ArrayList<Integer>();
		}
		this.commuStatus.add(v.intValue());
	}
	public Double calcCommuStatus() {
		if (this.commuStatus == null) {
			return 0.0;
		}
		int sum = 0;
		for (Integer s : this.commuStatus) {
			sum += s;
		}
		return (sum > 0 ? 1.0 : 0.0);
	}
}