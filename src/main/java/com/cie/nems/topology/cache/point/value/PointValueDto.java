package com.cie.nems.topology.cache.point.value;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.point.PointInfoDto;

public class PointValueDto {
	private Long _id;
	private Long pid;
	private String v;
	private Short vt;
	private Long dt;
	private Long t;
	private Integer q;
	private Double dv;
	private Integer iv;
	private String ext;

	private PointValueDto preValue;
	
	private PointInfoDto point;
	
	public PointValueDto() {
		super();
	}
	
	public PointValueDto(Long _id, Long pid, String v, Long dt, Long t, Integer q) {
		super();
		this._id = _id;
		this.pid = pid;
		this.v = v;
		this.dt = dt;
		this.t = t;
		this.q = q;
	}
	/**
	 * 主键，格式为pid+time
	 * time根据测点时间周期不同而不同：
	 * 实时：HHmmss，yyyyMMdd从表名获取
	 * 小时：MMddHH，yyyy从表名获取
	 * 日：yyMMdd
	 * 月：yyyyMM
	 * 年：yyyy
	 * @return
	 */
	public Long get_id() {
		return _id;
	}
	public void set_id(Long _id) {
		this._id = _id;
	}
	/**
	 * 测点ID
	 */
	public Long getPid() {
		return pid;
	}
	public void setPid(Long pid) {
		this.pid = pid;
	}
	/**
	 * 数据时间戳，根据DbName、collectionName和_id组合转换得到
	 */
	public Long getDt() {
		return dt;
	}
	public void setDt(Long dt) {
		this.dt = dt;
	}
	/**
	 * 入库时间与数据时间之差（精确到秒）
	 */
	public Long getT() {
		return t;
	}
	public void setT(Long t) {
		this.t = t;
	}
	/**
	 * 标识位，通过编码可以表示数据质量等信息
	 * 0 正常数据，1非法数据(NaN或Infinity)
	 */
	public Integer getQ() {
		return q;
	}
	public void setQ(Integer q) {
		this.q = q;
	}
	public Short getVt() {
		return vt;
	}
	public void setVt(Short vt) {
		this.vt = vt;
	}
	public Double getDv() {
		return dv;
	}
	public void setDv(Double dv) {
		this.dv = dv;
	}
	public Integer getIv() {
		return iv;
	}
	public void setIv(Integer iv) {
		this.iv = iv;
	}
	public String getV() {
		return v;
	}
	/**
	 * 测点值，infinity和NaN表示为0，通过q字段标记
	 */
	public void setV(String v) {
		this.v = v;
	}
	public PointValueDto getPreValue() {
		return preValue;
	}
	public void setPreValue(PointValueDto preValue) {
		this.preValue = preValue;
	}

	public PointInfoDto getPoint() {
		return point;
	}
	public void setPoint(PointInfoDto point) {
		this.point = point;
	}

	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}
