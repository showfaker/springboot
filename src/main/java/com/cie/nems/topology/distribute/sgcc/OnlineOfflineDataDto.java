package com.cie.nems.topology.distribute.sgcc;

import java.util.List;

import com.cie.nems.common.service.CommonService;

public class OnlineOfflineDataDto {
	private List<Integer> stationId;
	public List<Integer> getStationId() {
		return stationId;
	}
	public void setStationId(List<Integer> stationId) {
		this.stationId = stationId;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
