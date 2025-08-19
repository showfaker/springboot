package com.cie.nems.test;

import java.io.Serializable;
import javax.persistence.*;
import java.sql.Timestamp;


/**
 * The persistent class for the point_data_real database table.
 * 
 */
@Entity
@Table(name="point_data_real")
@NamedQuery(name="PointDataReal.findAll", query="SELECT p FROM PointDataReal p")
public class PointDataReal implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="_id")
	private Long id;

	private Timestamp dt;

	private Long pid;

	private Integer q;

	private Timestamp t;

	private String v;

	private Integer vt;

	public PointDataReal() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getDt() {
		return this.dt;
	}

	public void setDt(Timestamp dt) {
		this.dt = dt;
	}

	public Long getPid() {
		return this.pid;
	}

	public void setPid(Long pid) {
		this.pid = pid;
	}

	public Integer getQ() {
		return this.q;
	}

	public void setQ(Integer q) {
		this.q = q;
	}

	public Timestamp getT() {
		return this.t;
	}

	public void setT(Timestamp t) {
		this.t = t;
	}

	public String getV() {
		return this.v;
	}

	public void setV(String v) {
		this.v = v;
	}

	public Integer getVt() {
		return this.vt;
	}

	public void setVt(Integer vt) {
		this.vt = vt;
	}

}