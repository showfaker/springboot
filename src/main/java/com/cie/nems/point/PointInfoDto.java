package com.cie.nems.point;

import com.cie.nems.common.service.CommonService;

public class PointInfoDto {
	private Long pointId;
	private String pointName;
	private String psrId;
	private String deviceId;
	private String areaId;
	private String stationId;
	private String customerId;
	private Integer calcChannel;
	private String remotionType;
	private String cateId;
	private String sysCateId;
	private String calcPeriod;
	private String dataPeriod;
	private String dataType;
	private Integer savePrecision;
	private String calcFormula;
	private String dbs;
	public Long getPointId() {
		return pointId;
	}
	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}
	public String getPointName() {
		return pointName;
	}
	public void setPointName(String pointName) {
		this.pointName = pointName;
	}
	public String getPsrId() {
		return psrId;
	}
	public void setPsrId(String psrId) {
		this.psrId = psrId;
	}
	public String getDeviceId() {
		return deviceId;
	}
	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}
	public String getAreaId() {
		return areaId;
	}
	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}
	public String getStationId() {
		return stationId;
	}
	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	public String getCustomerId() {
		return customerId;
	}
	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}
	public Integer getCalcChannel() {
		return calcChannel;
	}
	public void setCalcChannel(Integer calcChannel) {
		this.calcChannel = calcChannel;
	}
	public String getRemotionType() {
		return remotionType;
	}
	public void setRemotionType(String remotionType) {
		this.remotionType = remotionType;
	}
	public String getCateId() {
		return cateId;
	}
	public void setCateId(String cateId) {
		this.cateId = cateId;
	}
	public String getSysCateId() {
		return sysCateId;
	}
	public void setSysCateId(String sysCateId) {
		this.sysCateId = sysCateId;
	}
	public String getCalcPeriod() {
		return calcPeriod;
	}
	public void setCalcPeriod(String calcPeriod) {
		this.calcPeriod = calcPeriod;
	}
	public String getDataPeriod() {
		return dataPeriod;
	}
	public void setDataPeriod(String dataPeriod) {
		this.dataPeriod = dataPeriod;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	public Integer getSavePrecision() {
		return savePrecision;
	}
	public void setSavePrecision(Integer savePrecision) {
		this.savePrecision = savePrecision;
	}
	public String getCalcFormula() {
		return calcFormula;
	}
	public void setCalcFormula(String calcFormula) {
		this.calcFormula = calcFormula;
	}
	public String getDbs() {
		return dbs;
	}
	public void setDbs(String dbs) {
		this.dbs = dbs;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
