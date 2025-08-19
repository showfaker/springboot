package com.cie.nems.common.exception;

import java.util.List;

import org.springframework.validation.ObjectError;

public class NemsFormException extends Exception {
	private static final long serialVersionUID = 1L;
	private List<ObjectError> errorList;// 表单校验信息

	public List<ObjectError> getErrorList() {
		return errorList;
	}

	public void setErrorList(List<ObjectError> errorList) {
		this.errorList = errorList;
	}
}
