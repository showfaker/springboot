package com.cie.nems.common.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

/**
 * PvmsException数据对象
 * 
 * @author shh
 *
 */
public class NemsExceptionFormInfo extends NemsExceptionInfo {

	private List<ObjectError> errorList;// 表单校验信息

	public List<ObjectError> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<ObjectError> errorList) {
		this.errorList = errorList;
	}

}
