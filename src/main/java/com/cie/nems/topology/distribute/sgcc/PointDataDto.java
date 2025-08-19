package com.cie.nems.topology.distribute.sgcc;

import java.util.List;

import com.cie.nems.common.service.CommonService;
import com.fasterxml.jackson.annotation.JsonInclude;

public class PointDataDto {
	private Long pointId;
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Long timeStamp;
	private List<DataDto> dataArray;
	private Integer dataCount;
	public Long getPointId() {
		return pointId;
	}
	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}
	public Long getTimeStamp() {
		return timeStamp;
	}
	public void setTimeStamp(Long timeStamp) {
		this.timeStamp = timeStamp;
	}
	public List<DataDto> getDataArray() {
		return dataArray;
	}
	public void setDataArray(List<DataDto> dataArray) {
		this.dataArray = dataArray;
	}
	public Integer getDataCount() {
		return dataCount;
	}
	public void setDataCount(Integer dataCount) {
		this.dataCount = dataCount;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}