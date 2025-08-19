package com.cie.nems.topology.save.mongo;


import com.cie.nems.common.service.CommonService;

public class MongoSaveDto {
	/**
	 * 实时数据表nems_values_yyyyMMdd中，格式为pid+HH
	 * 小时数据表nems_values_hour_yyyy中，格式为pid+MMdd
	 * 日数据表nems_values_day中，格式为pid+YYMM
	 * 月数据表nems_values_month中，格式为pid+YY
	 * 年数据表nems_values_year中，格式为pid
	 */
	private long _id;
	private long pid;
	private String v;
	/**
	 * 实时数据表nems_values_yyyyMMdd中，格式为mmssSSS
	 * 小时数据表nems_values_hour_yyyy中，格式为HH
	 * 日数据表nems_values_day中，格式为dd
	 * 月数据表nems_values_month中，格式为MM
	 * 年数据表nems_values_year中，格式为yyyy
	 */
	private String dt;
	/**
	 * 系统当前时间减去PointValueDto中的dt
	 */
	private long t;
	private int q;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public long getPid() {
		return pid;
	}
	public void setPid(long pid) {
		this.pid = pid;
	}
	public String getV() {
		return v;
	}
	public void setV(String v) {
		this.v = v;
	}
	public String getDt() {
		return dt;
	}
	public void setDt(String dt) {
		this.dt = dt;
	}
	public long getT() {
		return t;
	}
	public void setT(long t) {
		this.t = t;
	}
	public int getQ() {
		return q;
	}
	public void setQ(int q) {
		this.q = q;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}
}
