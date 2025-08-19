package com.cie.nems.schedule;

import java.io.Serializable;
import javax.persistence.*;

import com.cie.nems.common.service.CommonService;

import java.sql.Timestamp;


/**
 * The persistent class for the schedule_job database table.
 * 
 */
@Entity
@Table(name="schedule_job")
@NamedQuery(name="ScheduleJob.findAll", query="SELECT s FROM ScheduleJob s")
public class ScheduleJob implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="job_id")
	private String jobId;

	@Column(name="job_ip")
	private String jobIp;

	@Column(name="job_app")
	private String jobApp;

	@Column(name="job_cron")
	private String jobCron;

	@Column(name="job_class")
	private String jobClass;

	@Column(name="job_method")
	private String jobMethod;

	@Column(name="job_status")
	private String jobStatus;

	@Column(name="job_name")
	private String jobName;

	@Column(name="job_memo")
	private String jobMemo;

	@Column(name="update_time")
	private Timestamp updateTime;

	private String updater;
	
	public String getJobId() {
		return jobId;
	}
	public void setJobId(String jobId) {
		this.jobId = jobId;
	}
	public String getJobIp() {
		return jobIp;
	}
	public void setJobIp(String jobIp) {
		this.jobIp = jobIp;
	}
	public String getJobApp() {
		return jobApp;
	}
	public void setJobApp(String jobApp) {
		this.jobApp = jobApp;
	}
	public String getJobCron() {
		return jobCron;
	}
	public void setJobCron(String jobCron) {
		this.jobCron = jobCron;
	}
	public String getJobClass() {
		return jobClass;
	}
	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}
	public String getJobMethod() {
		return jobMethod;
	}
	public void setJobMethod(String jobMethod) {
		this.jobMethod = jobMethod;
	}
	public String getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobMemo() {
		return jobMemo;
	}
	public void setJobMemo(String jobMemo) {
		this.jobMemo = jobMemo;
	}
	public Timestamp getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}
	public String getUpdater() {
		return updater;
	}
	public void setUpdater(String updater) {
		this.updater = updater;
	}
	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}