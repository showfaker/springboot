package com.cie.nems.common.exception;

public class NemsException extends Exception {
	private static final long serialVersionUID = -5513986536746077602L;
	
	private int code;
	private String message;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public NemsException() {
		super();
	}

	public NemsException(int code) {
		super();
		this.code = code;
	}

	public NemsException(String message) {
		super();
		this.message = message;
	}

	public NemsException(int code, String message) {
		super();
		this.code = code;
		this.message = message;
	}

}
