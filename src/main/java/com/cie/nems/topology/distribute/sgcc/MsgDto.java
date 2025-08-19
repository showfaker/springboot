package com.cie.nems.topology.distribute.sgcc;

import com.cie.nems.common.service.CommonService;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class MsgDto {
	private String cmd;
	private String ip;
	private Long timestamp;
	private String id;
	@JsonProperty
	private DataListDto data;
	
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@JsonIgnore
	public DataListDto getData() {
		return data;
	}
	@JsonIgnore
	public void setData(DataListDto data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}