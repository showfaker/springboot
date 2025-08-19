package com.cie.nems.topology.distribute.ly;

import com.cie.nems.common.service.CommonService;

public class LastValueDto {
	private Long dt;
	private String v;
	
	public LastValueDto() {
		super();
	}
	public LastValueDto(Long dt, String v) {
		super();
		this.dt = dt;
		this.v = v;
	}
	
	public Long getDt() {
		return dt;
	}
	public void setDt(Long dt) {
		this.dt = dt;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
