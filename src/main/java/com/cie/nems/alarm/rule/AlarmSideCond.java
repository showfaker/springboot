package com.cie.nems.alarm.rule;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import com.cie.nems.common.service.CommonService;


/**
 * The persistent class for the alarm_side_cond database table.
 * 
 */
@Entity
@Table(name="alarm_side_cond")
@NamedQuery(name="AlarmSideCond.findAll", query="SELECT a FROM AlarmSideCond a")
public class AlarmSideCond implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="cond_id")
	private Long condId;

	@Column(name="compare_symbol")
	private String compareSymbol;

	@Column(name="compare_val")
	private String compareVal;

	private Integer duration;

	@Column(name="point_id")
	private Long pointId;

	@Column(name="rule_id")
	private Long ruleId;

	@Transient
	private List<String> compareVals;

	@Transient
	private Double doubleCompareVal;
	
	@Transient
	private List<Double> doubleCompareVals;
	
	public AlarmSideCond() {
	}

	public Long getCondId() {
		return this.condId;
	}

	public void setCondId(Long condId) {
		this.condId = condId;
	}

	public String getCompareSymbol() {
		return this.compareSymbol;
	}

	public void setCompareSymbol(String compareSymbol) {
		this.compareSymbol = compareSymbol;
	}

	public String getCompareVal() {
		return this.compareVal;
	}

	public void setCompareVal(String compareVal) {
		this.compareVal = compareVal;
	}

	public Integer getDuration() {
		return this.duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public Long getPointId() {
		return this.pointId;
	}

	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}

	public Long getRuleId() {
		return this.ruleId;
	}

	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}

	public List<String> getCompareVals() {
		return compareVals;
	}

	public void setCompareVals(List<String> compareVals) {
		this.compareVals = compareVals;
	}

	public Double getDoubleCompareVal() {
		return doubleCompareVal;
	}

	public void setDoubleCompareVal(Double doubleCompareVal) {
		this.doubleCompareVal = doubleCompareVal;
	}

	public List<Double> getDoubleCompareVals() {
		return doubleCompareVals;
	}

	public void setDoubleCompareVals(List<Double> doubleCompareVals) {
		this.doubleCompareVals = doubleCompareVals;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}