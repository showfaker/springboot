package com.cie.nems.monitor.station;

import java.io.Serializable;
import javax.persistence.*;

import com.cie.nems.common.service.CommonService;

import java.util.Date;


/**
 * The persistent class for the station_monitor_real database table.
 * 
 */
@Entity
@Table(name="station_monitor_real")
@NamedQuery(name="StationMonitorReal.findAll", query="SELECT s FROM StationMonitorReal s")
public class StationMonitorReal implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="monitor_id")
	private String monitorId;

	private Double capacity;

	@Column(name="customer_id")
	private String customerId;

	@Column(name="energy_coll")
	private Double energyColl;

	@Column(name="energy_input")
	private Double energyInput;

	@Column(name="health_total")
	private Integer healthTotal;

	private Integer health1;

	private Integer health2;

	private Integer health3;

	private Integer health4;

	private Integer health5;

	@Column(name="max_power")
	private Double maxPower;

	@Column(name="max_power_time")
	private Date maxPowerTime;

	@Temporal(TemporalType.DATE)
	@Column(name="monitor_date")
	private Date monitorDate;

	private Double power;

	@Column(name="run_end_time")
	private Long runEndTime;

	@Column(name="run_start_time")
	private Long runStartTime;

	@Column(name="commu_status")
	private String commuStatus;

	@Column(name="run_status")
	private String runStatus;

	@Column(name="day_run_status")
	private String dayRunStatus;

	@Column(name="run_times")
	private Double runTimes;

	@Column(name="station_id")
	private String stationId;

	@Column(name="update_time")
	private Date updateTime;

	private Double irradiation;
	
	@Column(name="theoretic_energy")
	private Double theoreticEnergy;
	
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public Double getCapacity() {
		return this.capacity;
	}

	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Double getEnergyColl() {
		return this.energyColl;
	}

	public void setEnergyColl(Double energyColl) {
		this.energyColl = energyColl;
	}

	public Double getEnergyInput() {
		return this.energyInput;
	}

	public void setEnergyInput(Double energyInput) {
		this.energyInput = energyInput;
	}

	public Integer getHealthTotal() {
		return this.healthTotal;
	}

	public void setHealthTotal(Integer healthTotal) {
		this.healthTotal = healthTotal;
	}

	public Integer getHealth1() {
		return this.health1;
	}

	public void setHealth1(Integer health1) {
		this.health1 = health1;
	}

	public Integer getHealth2() {
		return this.health2;
	}

	public void setHealth2(Integer health2) {
		this.health2 = health2;
	}

	public Integer getHealth3() {
		return this.health3;
	}

	public void setHealth3(Integer health3) {
		this.health3 = health3;
	}

	public Integer getHealth4() {
		return this.health4;
	}

	public void setHealth4(Integer health4) {
		this.health4 = health4;
	}

	public Integer getHealth5() {
		return this.health5;
	}

	public void setHealth5(Integer health5) {
		this.health5 = health5;
	}

	public Double getMaxPower() {
		return this.maxPower;
	}

	public void setMaxPower(Double maxPower) {
		this.maxPower = maxPower;
	}

	public Date getMaxPowerTime() {
		return maxPowerTime;
	}

	public void setMaxPowerTime(Date maxPowerTime) {
		this.maxPowerTime = maxPowerTime;
	}

	public Date getMonitorDate() {
		return this.monitorDate;
	}

	public void setMonitorDate(Date monitorDate) {
		this.monitorDate = monitorDate;
	}

	public Double getPower() {
		return this.power;
	}

	public void setPower(Double power) {
		this.power = power;
	}

	public Long getRunEndTime() {
		return this.runEndTime;
	}

	public void setRunEndTime(Long runEndTime) {
		this.runEndTime = runEndTime;
	}

	public Long getRunStartTime() {
		return this.runStartTime;
	}

	public void setRunStartTime(Long runStartTime) {
		this.runStartTime = runStartTime;
	}

	public String getRunStatus() {
		return this.runStatus;
	}

	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}

	public String getDayRunStatus() {
		return dayRunStatus;
	}

	public void setDayRunStatus(String dayRunStatus) {
		this.dayRunStatus = dayRunStatus;
	}

	public Double getRunTimes() {
		return this.runTimes;
	}

	public void setRunTimes(Double runTimes) {
		this.runTimes = runTimes;
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

	public String getCommuStatus() {
		return commuStatus;
	}

	public void setCommuStatus(String commuStatus) {
		this.commuStatus = commuStatus;
	}

	public Double getIrradiation() {
		return irradiation;
	}

	public void setIrradiation(Double irradiation) {
		this.irradiation = irradiation;
	}

	public Double getTheoreticEnergy() {
		return theoreticEnergy;
	}

	public void setTheoreticEnergy(Double theoreticEnergy) {
		this.theoreticEnergy = theoreticEnergy;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}