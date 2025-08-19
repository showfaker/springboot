package com.cie.nems.topology;

import java.io.Serializable;
import javax.persistence.*;
import java.util.Date;


/**
 * The persistent class for the calc_topo_cfg database table.
 * 
 */
@Entity
@Table(name="calc_topo_cfg")
@NamedQuery(name="CalcTopoCfg.findAll", query="SELECT c FROM CalcTopoCfg c")
public class CalcTopoCfg implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="topo_id")
	private String topoId;

	@Column(name="channel_ids")
	private String channelIds;

	@Column(name="client_num")
	private Integer clientNum;

	@Column(name="client_prefix")
	private String clientPrefix;

	private String topic;

	@Column(name="topo_app")
	private String topoApp;

	@Column(name="topo_ip")
	private String topoIp;

	@Column(name="topo_status")
	private String topoStatus;

	@Column(name="update_time")
	private Date updateTime;

	private String updater;

	public CalcTopoCfg() {
	}

	public String getTopoId() {
		return this.topoId;
	}

	public void setTopoId(String topoId) {
		this.topoId = topoId;
	}

	public String getChannelIds() {
		return this.channelIds;
	}

	public void setChannelIds(String channelIds) {
		this.channelIds = channelIds;
	}

	public Integer getClientNum() {
		return this.clientNum;
	}

	public void setClientNum(Integer clientNum) {
		this.clientNum = clientNum;
	}

	public String getClientPrefix() {
		return this.clientPrefix;
	}

	public void setClientPrefix(String clientPrefix) {
		this.clientPrefix = clientPrefix;
	}

	public String getTopic() {
		return this.topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	public String getTopoApp() {
		return this.topoApp;
	}

	public void setTopoApp(String topoApp) {
		this.topoApp = topoApp;
	}

	public String getTopoIp() {
		return this.topoIp;
	}

	public void setTopoIp(String topoIp) {
		this.topoIp = topoIp;
	}

	public String getTopoStatus() {
		return this.topoStatus;
	}

	public void setTopoStatus(String topoStatus) {
		this.topoStatus = topoStatus;
	}

	public Date getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public String getUpdater() {
		return this.updater;
	}

	public void setUpdater(String updater) {
		this.updater = updater;
	}

}