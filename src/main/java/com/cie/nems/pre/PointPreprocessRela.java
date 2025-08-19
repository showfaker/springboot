package com.cie.nems.pre;

import com.cie.nems.common.service.CommonService;

public class PointPreprocessRela {
	private Long pointId;
	private Long ruleId;
	public Long getPointId() {
		return pointId;
	}
	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}
	public Long getRuleId() {
		return ruleId;
	}
	public void setRuleId(Long ruleId) {
		this.ruleId = ruleId;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}