package com.cie.nems.alarm.filter;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the alarm_filter database table.
 * 
 */
@Entity
@Table(name="alarm_filter")
@NamedQuery(name="AlarmFilter.findAll", query="SELECT a FROM AlarmFilter a")
public class AlarmFilter implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="filter_id")
	private Long filterId;

	@Column(name="device_id")
	private String deviceId;

	@Column(name="end_time")
	private Date endTime;

	@Column(name="start_time")
	private Date startTime;

	@Column(name="station_id")
	private String stationId;

	@Column(name="update_time")
	private Date updateTime;

	private String updater;

	public AlarmFilter() {
	}

	public Long getFilterId() {
		return this.filterId;
	}

	public void setFilterId(Long filterId) {
		this.filterId = filterId;
	}

	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public String getStationId() {
		return this.stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public Date getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdater() {
		return this.updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

}