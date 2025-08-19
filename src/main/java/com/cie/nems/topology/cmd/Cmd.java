package com.cie.nems.topology.cmd;

public enum Cmd {
	updateStationInfo("updateStationInfo"),
	deleteStationInfo("deleteStationInfo"),
	updateDeviceInfo("updateDeviceInfo"),
	deleteDeviceInfo("deleteDeviceInfo"),
	updatePointInfo("updatePointInfo"),
	deletePointInfo("deletePointInfo");

	private final String name;
	Cmd(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
}
