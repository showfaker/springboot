package com.cie.nems.common.parameter;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.*;

/**
 * The persistent class for the sys_parameters database table.
 * 
 */
@Entity
@Table(name = "app_parameter")
@NamedQuery(name = "AppParameter.findAll", query = "SELECT s FROM AppParameter s")
public class AppParameter implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "param_code")
	private String paramCode;

	@Column(name = "param_name")
	private String paramName;

	@Column(name = "param_type")
	private String paramType;

	@Transient
	private String paramTypeText;

	@Column(name = "param1")
	private String param1;

	@Column(name = "param2")
	private String param2;

	@Column(name = "param3")
	private String param3;

	@Column(name = "param4")
	private String param4;

	@Column(name = "param5")
	private String param5;

	@Column(name = "memo")
	private String memo;

	@Column(name="update_time")
	private Timestamp updateTime;

	private String updater;

	public AppParameter() {
	}

	public String getParamCode() {
		return this.paramCode;
	}

	public void setParamCode(String paramCode) {
		this.paramCode = paramCode;
	}

	public String getParamType() {
		return this.paramType;
	}

	public void setParamType(String paramType) {
		this.paramType = paramType;
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

	public String getParam5() {
		return param5;
	}

	public void setParam5(String param5) {
		this.param5 = param5;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdater() {
		return updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

	public String getParamTypeText() {
		return paramTypeText;
	}

	public void setParamTypeText(String paramTypeText) {
		this.paramTypeText = paramTypeText;
	}

	@Override
	public String toString() {
		return "{paramCode: " + paramCode + ", paramName: " + paramName + ", paramType: " + paramType
				+ ", paramTypeText: " + paramTypeText + ", param1: " + param1 + ", param2: " + param2 + ", param3: "
				+ param3 + ", param4: " + param4 + ", param5: " + param5 + ", memo: " + memo + ", updateTime: "
				+ updateTime + ", updater: " + updater + "}";
	}

}