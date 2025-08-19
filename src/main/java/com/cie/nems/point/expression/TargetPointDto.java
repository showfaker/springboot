package com.cie.nems.point.expression;

import com.cie.nems.common.service.CommonService;

public class TargetPointDto {
	/**
	 * 存放计算结果的测点ID
	 */
	private Long pointId;
	/**
	 * 计算结果保留小数位数，如果为空，则按point_category.save_precision的定义截取
	 */
	private Integer precision;
	/**
	 * 计算结果的合理值区间下限，小于该值会被强制覆盖为该值</br>
	 * 例如：</br>
	 * 如果公式计算结果为25，validFloor配置为30，则返回30</br>
	 * 如果公式计算结果为31，validFloor配置为30，则返回31
	 */
	private Double validFloor;
	/**
	 * 计算结果的合理值区间上限，大于该值会被强制覆盖为该值</br>
	 * 例如：</br>
	 * 如果公式计算结果为45，validCeil配置为40，则返回40</br>
	 * 如果公式计算结果为39，validCeil配置为40，则返回39
	 */
	private Double validCeil;
	/**
	 * 如果需要同步到特定中间表，在此处填写表名，例如station_monitor_real</br>
	 * 如果不需要同步，则不填写
	 */
	private String monitorTable;
	/**
	 * 当moitorTable不为空时，用本属性指定列名
	 */
	private String monitorColumn;
	
	public Long getPointId() {
		return pointId;
	}
	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}
	public Integer getPrecision() {
		return precision;
	}
	public void setPrecision(Integer precision) {
		this.precision = precision;
	}
	public Double getValidFloor() {
		return validFloor;
	}
	public void setValidFloor(Double validFloor) {
		this.validFloor = validFloor;
	}
	public Double getValidCeil() {
		return validCeil;
	}
	public void setValidCeil(Double validCeil) {
		this.validCeil = validCeil;
	}
	public String getMonitorTable() {
		return monitorTable;
	}
	public void setMonitorTable(String monitorTable) {
		this.monitorTable = monitorTable;
	}
	public String getMonitorColumn() {
		return monitorColumn;
	}
	public void setMonitorColumn(String monitorColumn) {
		this.monitorColumn = monitorColumn;
	}
	
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
