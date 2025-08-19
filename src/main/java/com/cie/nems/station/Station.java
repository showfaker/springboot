package com.cie.nems.station;

import java.util.LinkedList;
import java.util.List;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;

public class Station {
	private String stationId;
	private String shortName;
	private String psrId;
	private String customerId;
	private Double capacity;
	private Double parallelCapacity;
	private Integer calcChannel;
	private String powerSource;
	private String energySource;
	private String countryId;
	private String provinceId;
	private String cityId;
	private String countyId;
	
	private List<Device> meters = new LinkedList<Device>();
	private List<Device> inverters = new LinkedList<Device>();;
	
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getShortName() {
		return shortName;
	}
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	public String getPsrId() {
		return psrId;
	}
	public void setPsrId(String psrId) {
		this.psrId = psrId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public Double getCapacity() {
		return capacity;
	}
	public void setCapacity(Double capacity) {
		this.capacity = capacity;
	}
	public Double getParallelCapacity() {
		return parallelCapacity;
	}
	public void setParallelCapacity(Double parallelCapacity) {
		this.parallelCapacity = parallelCapacity;
	}
	public Integer getCalcChannel() {
		return calcChannel;
	}
	public void setCalcChannel(Integer calcChannel) {
		this.calcChannel = calcChannel;
	}
	public String getPowerSource() {
		return powerSource;
	}
	public void setPowerSource(String powerSource) {
		this.powerSource = powerSource;
	}
	public String getEnergySource() {
		return energySource;
	}
	public void setEnergySource(String energySource) {
		this.energySource = energySource;
	}
	public List<Device> getMeters() {
		return meters;
	}
	public void setMeters(List<Device> meters) {
		this.meters = meters;
	}
	public List<Device> getInverters() {
		return inverters;
	}
	public void setInverters(List<Device> inverters) {
		this.inverters = inverters;
	}
	public String getCountryId() {
		return countryId;
	}
	public void setCountryId(String countryId) {
		this.countryId = countryId;
	}
	public String getProvinceId() {
		return provinceId;
	}
	public void setProvinceId(String provinceId) {
		this.provinceId = provinceId;
	}
	public String getCityId() {
		return cityId;
	}
	public void setCityId(String cityId) {
		this.cityId = cityId;
	}
	public String getCountyId() {
		return countyId;
	}
	public void setCountyId(String countyId) {
		this.countyId = countyId;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
