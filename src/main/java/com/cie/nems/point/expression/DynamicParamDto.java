package com.cie.nems.point.expression;

import com.cie.nems.common.service.CommonService;

public class DynamicParamDto {
	/**
	 * 用于定义从什么对象取什么属性的值， 定义在ExpressionService的静态变量中</br>
	 * station.capacity</br>
	 * station.parallelCapacity</br>
	 */
	private String objType;
	
	private String objId;
	
	public String getObjType() {
		return objType;
	}
	public void setObjType(String objType) {
		this.objType = objType;
	}
	public String getObjId() {
		return objId;
	}
	public void setObjId(String objId) {
		this.objId = objId;
	}
	
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
