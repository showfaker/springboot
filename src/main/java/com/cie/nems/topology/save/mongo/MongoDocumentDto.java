package com.cie.nems.topology.save.mongo;

import java.util.Map;

import com.cie.nems.common.service.CommonService;

public class MongoDocumentDto {
	private long _id;
	private Map<String, String> value;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public Map<String, String> getValue() {
		return value;
	}
	public void setValue(Map<String, String> value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
