package com.cie.nems.common.exception;

import com.cie.nems.common.service.CommonService;

public class ExceptionInfoDto {
	private String data;
	private long lastTime;
	private long count;
	private String errorMessage;
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public long getLastTime() {
		return lastTime;
	}
	public void setLastTime(long lastTime) {
		this.lastTime = lastTime;
	}
	public long getCount() {
		return count;
	}
	public void setCount(long count) {
		this.count = count;
	}
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
