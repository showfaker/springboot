package com.cie.nems.pre;

import com.cie.nems.common.service.CommonService;

public class PointPreprocessRule {
	private Long ruleId;
	private String param1;
	private String param2;
	private String param3;
	private String param4;
	private Double doubleParam1;
	private Double doubleParam2;
	private Double doubleParam3;
	private Double doubleParam4;
	private Long longParam1;
	private Long longParam2;
	private Long longParam3;
	private Long longParam4;
	private String ruleType;
	private Integer quality;
	public Integer getQuality() {
		return quality;
	}
	public void setQuality(Integer quality) {
		this.quality = quality;
	}
	public Long getRuleId() {
		return ruleId;
	}
	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
	public String getParam1() {
		return param1;
	}
	public void setParam1(String param1) {
		this.param1 = param1;
	}
	public String getParam2() {
		return param2;
	}
	public void setParam2(String param2) {
		this.param2 = param2;
	}
	public String getParam3() {
		return param3;
	}
	public void setParam3(String param3) {
		this.param3 = param3;
	}
	public String getParam4() {
		return param4;
	}
	public void setParam4(String param4) {
		this.param4 = param4;
	}
	public String getRuleType() {
		return ruleType;
	}
	public void setRuleType(String ruleType) {
		this.ruleType = ruleType;
	}
	public Double getDoubleParam1() {
		return doubleParam1;
	}
	public void setDoubleParam1(Double doubleParam1) {
		this.doubleParam1 = doubleParam1;
	}
	public Double getDoubleParam2() {
		return doubleParam2;
	}
	public void setDoubleParam2(Double doubleParam2) {
		this.doubleParam2 = doubleParam2;
	}
	public Double getDoubleParam3() {
		return doubleParam3;
	}
	public void setDoubleParam3(Double doubleParam3) {
		this.doubleParam3 = doubleParam3;
	}
	public Double getDoubleParam4() {
		return doubleParam4;
	}
	public void setDoubleParam4(Double doubleParam4) {
		this.doubleParam4 = doubleParam4;
	}
	public Long getLongParam1() {
		return longParam1;
	}
	public void setLongParam1(Long longParam1) {
		this.longParam1 = longParam1;
	}
	public Long getLongParam2() {
		return longParam2;
	}
	public void setLongParam2(Long longParam2) {
		this.longParam2 = longParam2;
	}
	public Long getLongParam3() {
		return longParam3;
	}
	public void setLongParam3(Long longParam3) {
		this.longParam3 = longParam3;
	}
	public Long getLongParam4() {
		return longParam4;
	}
	public void setLongParam4(Long longParam4) {
		this.longParam4 = longParam4;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
