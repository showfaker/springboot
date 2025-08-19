package com.cie.nems.topology.cmd;

import com.cie.nems.common.service.CommonService;

public class CmdDto {
	private Cmd cmd;
	private String info;
	public Cmd getCmd() {
		return cmd;
	}
	public void setCmd(Cmd cmd) {
		this.cmd = cmd;
	}
	public String getInfo() {
		return info;
	}
	public void setInfo(String info) {
		this.info = info;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
