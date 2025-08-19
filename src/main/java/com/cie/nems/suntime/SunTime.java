package com.cie.nems.suntime;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the sun_time database table.
 * 
 */
@Entity
@Table(name="sun_time")
@NamedQuery(name="SunTime.findAll", query="SELECT s FROM SunTime s")
public class SunTime implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="sun_time_id")
	private String sunTimeId;

	@Column(name="end_date")
	private Integer endDate;

	@Column(name="region_id")
	private String regionId;

	@Column(name="start_date")
	private Integer startDate;

	@Column(name="sunrise_time")
	private Integer sunriseTime;

	@Column(name="sunset_time")
	private Integer sunsetTime;

	@Column(name="update_time")
	private Timestamp updateTime;

	private String updater;

	public SunTime() {
	}

	public String getSunTimeId() {
		return this.sunTimeId;
	}

	public void setSunTimeId(String sunTimeId) {
		this.sunTimeId = sunTimeId;
	}

	public Integer getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Integer endDate) {
		this.endDate = endDate;
	}

	public String getRegionId() {
		return this.regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public Integer getStartDate() {
		return this.startDate;
	}

	public void setStartDate(Integer startDate) {
		this.startDate = startDate;
	}

	public Integer getSunriseTime() {
		return this.sunriseTime;
	}

	public void setSunriseTime(Integer sunriseTime) {
		this.sunriseTime = sunriseTime;
	}

	public Integer getSunsetTime() {
		return this.sunsetTime;
	}

	public void setSunsetTime(Integer sunsetTime) {
		this.sunsetTime = sunsetTime;
	}

	public Timestamp getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdater() {
		return this.updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

}