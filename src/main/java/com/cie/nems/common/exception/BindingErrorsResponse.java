package com.cie.nems.common.exception;

import java.util.ArrayList;
import java.util.List;

import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BindingErrorsResponse {

	private List<BindingError> bindingErrors = new ArrayList<BindingError>();

	public List<BindingError> getBindingErrors() {
		return bindingErrors;
	}

	public void setBindingErrors(List<BindingError> bindingErrors) {
		this.bindingErrors = bindingErrors;
	}

	public void addError(BindingError bindingError) {
		this.bindingErrors.add(bindingError);
	}

	public void addAllErrors(BindingResult bindingResult) {
		for (FieldError fieldError : bindingResult.getFieldErrors()) {
			BindingError error = new BindingError();
			error.setObjectName(fieldError.getObjectName());
			error.setFieldName(fieldError.getField());
			error.setFieldValue(
					fieldError.getRejectedValue() == null ? null : fieldError.getRejectedValue().toString());
			error.setErrorMessage(fieldError.getDefaultMessage());
			error.setErrorCode(fieldError.getCode());

			addError(error);
		}
	}

	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		mapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
		String errorsAsJSON = "";
		try {
			errorsAsJSON = mapper.writeValueAsString(bindingErrors);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return errorsAsJSON;
	}

	@Override
	public String toString() {
		return "BindingErrorsResponse [bindingErrors=" + bindingErrors + "]";
	}

	protected class BindingError {

		private String objectName;
		private String fieldName;
		private String fieldValue;
		private String errorMessage;
		private String errorCode;

		public BindingError() {
			this.objectName = "";
			this.fieldName = "";
			this.fieldValue = "";
			this.errorMessage = "";
		}

		protected String getObjectName() {
			return objectName;
		}

		protected void setObjectName(String objectName) {
			this.objectName = objectName;
		}

		protected String getFieldName() {
			return fieldName;
		}

		protected void setFieldName(String fieldName) {
			this.fieldName = fieldName;
		}

		protected String getFieldValue() {
			return fieldValue;
		}

		protected void setFieldValue(String fieldValue) {
			this.fieldValue = fieldValue;
		}

		protected String getErrorMessage() {
			return errorMessage;
		}

		protected void setErrorMessage(String error_message) {
			this.errorMessage = error_message;
		}
		
		protected String getErrorCode() {
			return errorCode;
		}

		protected void setErrorCode(String errorCode) {
			this.errorCode = errorCode;
		}

		@Override
		public String toString() {
			return "BindingError [objectName=" + objectName + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue
					+ ", errorCode=" + errorCode + ", errorMessage=" + errorMessage + "]";
		}

	}

}