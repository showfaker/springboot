package com.cie.nems.topology.distribute.sgcc;

import com.cie.nems.common.service.CommonService;

public class OnlineOfflineDto {
	//2.1  1.0前置服务注册成功后往kafka写消息格式
	//kafka msg = {"ip":"127.0.0.1:40278","cmd":"0x01","data":{"stationId":[1001,1002,1003,1004]},"timestamp":1537103351129}
	//2.2  1.0前置服务在某个连接断开后往kafka写消息格式
	//kafka msg = {"ip":"127.0.0.1:40278","cmd":"0xFF","data":{"stationId":[1001,1002,1003,1004]},"timestamp":1537103351129}
	//2.3  2.0前置服务注册成功后往kafka写消息格式
	//kafka msg = {"id":"123456","ip":"127.0.0.1:40278","cmd":"0x01","data":{"stationId":[1001,1002,1003,1004]},"timestamp":1537103351129}
	//2.4  2.0前置服务在某个连接断开后往kafka写消息格式
	//kafka msg = {"id":"123456","ip":"127.0.0.1:40278","cmd":"0xFF","data":{"stationId":[1001,1002,1003,1004]},"timestamp":1537103351129}
	private String ip;
	private String cmd;
	private String id;
	private Long timestamp;
	private OnlineOfflineDataDto data;
	
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public Long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(Long timestamp) {
		this.timestamp = timestamp;
	}
	public OnlineOfflineDataDto getData() {
		return data;
	}
	public void setData(OnlineOfflineDataDto data) {
		this.data = data;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
