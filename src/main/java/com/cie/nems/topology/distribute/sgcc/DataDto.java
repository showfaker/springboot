package com.cie.nems.topology.distribute.sgcc;

import com.cie.nems.common.service.CommonService;

public class DataDto {
	private Short dataId;
	private String valueType;
	private String value;
	private Float quality;
	public DataDto() {
		super();
	}
	public DataDto(Short dataId, String valueType, String value, Float quality) {
		super();
		this.dataId = dataId;
		this.valueType = valueType;
		this.value = value;
		this.quality = quality;
	}
	public Short getDataId() {
		return dataId;
	}
	public void setDataId(Short dataId) {
		this.dataId = dataId;
	}
	public String getValueType() {
		return valueType;
	}
	public void setValueType(String valueType) {
		this.valueType = valueType;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public Float getQuality() {
		return quality;
	}
	public void setQuality(Float quality) {
		this.quality = quality;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}