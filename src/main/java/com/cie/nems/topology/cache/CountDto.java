package com.cie.nems.topology.cache;

import com.cie.nems.common.service.CommonService;

public class CountDto {
	private int deviceCount;
	private int deviceAll;
	private int objRelaCount;
	private int objRelaAll;
	private int pointCount;
	private int pointAll;
	private int expCount;
	private int expAll;

	public int getDeviceCount() {
		return deviceCount;
	}

	public void setDeviceCount(int deviceCount) {
		this.deviceCount = deviceCount;
	}

	public int getDeviceAll() {
		return deviceAll;
	}

	public void addDeviceAll(int deviceAll) {
		this.deviceAll += deviceAll;
	}

	public int getObjRelaCount() {
		return objRelaCount;
	}

	public void setObjRelaCount(int objRelaCount) {
		this.objRelaCount = objRelaCount;
	}

	public int getObjRelaAll() {
		return objRelaAll;
	}

	public void addObjRelaAll(int objRelaAll) {
		this.objRelaAll += objRelaAll;
	}

	public int getPointCount() {
		return pointCount;
	}

	public void setPointCount(int pointCount) {
		this.pointCount = pointCount;
	}

	public int getPointAll() {
		return pointAll;
	}

	public void addPointAll(int pointAll) {
		this.pointAll += pointAll;
	}

	public int getExpCount() {
		return expCount;
	}

	public void setExpCount(int expCount) {
		this.expCount = expCount;
	}

	public int getExpAll() {
		return expAll;
	}

	public void addExpAll(int expAll) {
		this.expAll += expAll;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}
	
}
