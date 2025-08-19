package com.cie.nems.objRela;

import com.cie.nems.common.service.CommonService;


public class ObjRela {
	private String objId1;
	private String objId2;
	private Long pointId;
	private String param1;
	private String psrId1;
	private String psrId2;

	public ObjRela() {
	}

	public String getObjId1() {
		return objId1;
	}

	public void setObjId1(String objId1) {
		this.objId1 = objId1;
	}

	public String getObjId2() {
		return objId2;
	}

	public void setObjId2(String objId2) {
		this.objId2 = objId2;
	}

	public Long getPointId() {
		return this.pointId;
	}

	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}

	public String getParam1() {
		return param1;
	}

	public void setParam1(String param1) {
		this.param1 = param1;
	}

	public String getPsrId1() {
		return psrId1;
	}

	public void setPsrId1(String psrId1) {
		this.psrId1 = psrId1;
	}

	public String getPsrId2() {
		return psrId2;
	}

	public void setPsrId2(String psrId2) {
		this.psrId2 = psrId2;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}