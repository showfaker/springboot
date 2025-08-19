package com.cie.nems.topology.distribute.sgcc;

import java.util.List;

import com.cie.nems.common.service.CommonService;
import com.fasterxml.jackson.annotation.JsonInclude;

public class DataListDto {
	private Integer stationId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long timeStamp;
	private Short realtimeType;
	private Integer pointCount;
	private List<PointDataDto> pointDataArray;
	public Integer getStationId() {
		return stationId;
	}
	public void setStationId(Integer stationId) {
		this.stationId = stationId;
	}
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public Short getRealtimeType() {
		return realtimeType;
	}
	public void setRealtimeType(Short realtimeType) {
		this.realtimeType = realtimeType;
	}
	public Integer getPointCount() {
		return pointCount;
	}
	public void setPointCount(Integer pointCount) {
		this.pointCount = pointCount;
	}
	public List<PointDataDto> getPointDataArray() {
		return pointDataArray;
	}
	public void setPointDataArray(List<PointDataDto> pointDataArray) {
		this.pointDataArray = pointDataArray;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}