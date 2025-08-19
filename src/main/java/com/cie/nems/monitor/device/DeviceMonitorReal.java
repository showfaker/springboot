package com.cie.nems.monitor.device;

import java.io.Serializable;
import javax.persistence.*;

import com.cie.nems.common.service.CommonService;

import java.util.Date;


/**
 * The persistent class for the device_monitor_real database table.
 * 
 */
@Entity
@Table(name="device_monitor_real")
@NamedQuery(name="DeviceMonitorReal.findAll", query="SELECT d FROM DeviceMonitorReal d")
public class DeviceMonitorReal implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="monitor_id")
	private String monitorId;

	@Column(name="commu_status")
	private String commuStatus;

	@Column(name="commu_status_time")
	private Date commuStatusTime;

	@Column(name="last_data_time")
	private Date lastDataTime;

	@Column(name="customer_id")
	private String customerId;

	@Column(name="day_alarms")
	private Integer dayAlarms;

	@Column(name="day_offline_dur")
	private Integer dayOfflineDur;

	@Column(name="day_offline_num")
	private Integer dayOfflineNum;

	@Column(name="day_stop_num")
	private Integer dayStopNum;

	@Column(name="device_id")
	private String deviceId;

	@Column(name="fault_stop_duration")
	private Integer faultStopDuration;

	@Column(name="last_alarm_time")
	private Date lastAlarmTime;

	@Temporal(TemporalType.DATE)
	@Column(name="monitor_date")
	private Date monitorDate;

	@Column(name="real_alarms")
	private Integer realAlarms;

	@Column(name="real_alarms_time")
	private Date realAlarmsTime;

	@Column(name="run_end_time")
	private Long runEndTime;

	@Column(name="run_start_time")
	private Long runStartTime;

	@Column(name="run_status")
	private String runStatus;

	@Column(name="day_run_status")
	private String dayRunStatus;

	@Column(name="run_status_time")
	private Date runStatusTime;

	@Column(name="run_times")
	private Double runTimes;

	@Column(name="station_id")
	private String stationId;

	@Column(name="uncheck_alarms")
	private Integer uncheckAlarms;

	@Column(name="uncheck_alarms_time")
	private Date uncheckAlarmsTime;

	@Column(name="update_time")
	private Date updateTime;
	
	private Double value1;
	private Double value2;
	private Double value3;
	private Double value4;
	private Double value5;
	private Double value6;
	private Double value7;
	private Double value8;
	private Double value9;
	private Double value10;
	private Double value11;
	private Double value12;
	private Double value13;
	private Double value14;
	private Double value15;
	private Double value16;
	private Double value17;
	private Double value18;
	private Double value19;
	private Double value20;
	private Double value21;
	private Double value22;
	private Double value23;
	private Double value24;
	private Double value25;
	private Double value26;
	private Double value27;
	private Double value28;
	private Double value29;
	private Double value30;
	private Double value31;
	private Double value32;
	private Double value33;
	private Double value34;
	private Double value35;
	private Double value36;
	private Double value37;
	private Double value38;
	private Double value39;
	private Double value40;
	private Double value41;
	private Double value42;
	private Double value43;
	private Double value44;
	private Double value45;
	private Double value46;
	private Double value47;
	private Double value48;
	private Double value49;
	private Double value50;
	
	public String getMonitorId() {
		return this.monitorId;
	}

	public void setMonitorId(String monitorId) {
		this.monitorId = monitorId;
	}

	public String getCommuStatus() {
		return this.commuStatus;
	}

	public void setCommuStatus(String commuStatus) {
		this.commuStatus = commuStatus;
	}

	public Date getCommuStatusTime() {
		return this.commuStatusTime;
	}

	public void setCommuStatusTime(Date commuStatusTime) {
		this.commuStatusTime = commuStatusTime;
	}

	public Date getLastDataTime() {
		return lastDataTime;
	}

	public void setLastDataTime(Date lastDataTime) {
		this.lastDataTime = lastDataTime;
	}

	public String getCustomerId() {
		return this.customerId;
	}

	public void setCustomerId(String customerId) {
		this.customerId = customerId;
	}

	public Integer getDayAlarms() {
		return this.dayAlarms;
	}

	public void setDayAlarms(Integer dayAlarms) {
		this.dayAlarms = dayAlarms;
	}

	public Integer getDayOfflineDur() {
		return this.dayOfflineDur;
	}

	public void setDayOfflineDur(Integer dayOfflineDur) {
		this.dayOfflineDur = dayOfflineDur;
	}

	public Integer getDayOfflineNum() {
		return this.dayOfflineNum;
	}

	public void setDayOfflineNum(Integer dayOfflineNum) {
		this.dayOfflineNum = dayOfflineNum;
	}

	public Integer getDayStopNum() {
		return this.dayStopNum;
	}

	public void setDayStopNum(Integer dayStopNum) {
		this.dayStopNum = dayStopNum;
	}

	public String getDeviceId() {
		return this.deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public Integer getFaultStopDuration() {
		return this.faultStopDuration;
	}

	public void setFaultStopDuration(Integer faultStopDuration) {
		this.faultStopDuration = faultStopDuration;
	}

	public Date getLastAlarmTime() {
		return this.lastAlarmTime;
	}

	public void setLastAlarmTime(Date lastAlarmTime) {
		this.lastAlarmTime = lastAlarmTime;
	}

	public Date getMonitorDate() {
		return this.monitorDate;
	}

	public void setMonitorDate(Date monitorDate) {
		this.monitorDate = monitorDate;
	}

	public Integer getRealAlarms() {
		return this.realAlarms;
	}

	public void setRealAlarms(Integer realAlarms) {
		this.realAlarms = realAlarms;
	}

	public Date getRealAlarmsTime() {
		return this.realAlarmsTime;
	}

	public void setRealAlarmsTime(Date realAlarmsTime) {
		this.realAlarmsTime = realAlarmsTime;
	}

	public Long getRunEndTime() {
		return this.runEndTime;
	}

	public void setRunEndTime(Long runEndTime) {
		this.runEndTime = runEndTime;
	}

	public Long getRunStartTime() {
		return this.runStartTime;
	}

	public void setRunStartTime(Long runStartTime) {
		this.runStartTime = runStartTime;
	}

	public String getRunStatus() {
		return this.runStatus;
	}

	public void setRunStatus(String runStatus) {
		this.runStatus = runStatus;
	}

	public String getDayRunStatus() {
		return dayRunStatus;
	}

	public void setDayRunStatus(String dayRunStatus) {
		this.dayRunStatus = dayRunStatus;
	}

	public Date getRunStatusTime() {
		return this.runStatusTime;
	}

	public void setRunStatusTime(Date runStatusTime) {
		this.runStatusTime = runStatusTime;
	}

	public Double getRunTimes() {
		return this.runTimes;
	}

	public void setRunTimes(Double runTimes) {
		this.runTimes = runTimes;
	}

	public String getStationId() {
		return this.stationId;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}

	public Integer getUncheckAlarms() {
		return this.uncheckAlarms;
	}

	public void setUncheckAlarms(Integer uncheckAlarms) {
		this.uncheckAlarms = uncheckAlarms;
	}

	public Date getUncheckAlarmsTime() {
		return this.uncheckAlarmsTime;
	}

	public void setUncheckAlarmsTime(Date uncheckAlarmsTime) {
		this.uncheckAlarmsTime = uncheckAlarmsTime;
	}

	public Date getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Double getValue1() {
		return value1;
	}

	public void setValue1(Double value1) {
		this.value1 = value1;
	}

	public Double getValue2() {
		return value2;
	}

	public void setValue2(Double value2) {
		this.value2 = value2;
	}

	public Double getValue3() {
		return value3;
	}

	public void setValue3(Double value3) {
		this.value3 = value3;
	}

	public Double getValue4() {
		return value4;
	}

	public void setValue4(Double value4) {
		this.value4 = value4;
	}

	public Double getValue5() {
		return value5;
	}

	public void setValue5(Double value5) {
		this.value5 = value5;
	}

	public Double getValue6() {
		return value6;
	}

	public void setValue6(Double value6) {
		this.value6 = value6;
	}

	public Double getValue7() {
		return value7;
	}

	public void setValue7(Double value7) {
		this.value7 = value7;
	}

	public Double getValue8() {
		return value8;
	}

	public void setValue8(Double value8) {
		this.value8 = value8;
	}

	public Double getValue9() {
		return value9;
	}

	public void setValue9(Double value9) {
		this.value9 = value9;
	}

	public Double getValue10() {
		return value10;
	}

	public void setValue10(Double value10) {
		this.value10 = value10;
	}

	public Double getValue11() {
		return value11;
	}

	public void setValue11(Double value11) {
		this.value11 = value11;
	}

	public Double getValue12() {
		return value12;
	}

	public void setValue12(Double value12) {
		this.value12 = value12;
	}

	public Double getValue13() {
		return value13;
	}

	public void setValue13(Double value13) {
		this.value13 = value13;
	}

	public Double getValue14() {
		return value14;
	}

	public void setValue14(Double value14) {
		this.value14 = value14;
	}

	public Double getValue15() {
		return value15;
	}

	public void setValue15(Double value15) {
		this.value15 = value15;
	}

	public Double getValue16() {
		return value16;
	}

	public void setValue16(Double value16) {
		this.value16 = value16;
	}

	public Double getValue17() {
		return value17;
	}

	public void setValue17(Double value17) {
		this.value17 = value17;
	}

	public Double getValue18() {
		return value18;
	}

	public void setValue18(Double value18) {
		this.value18 = value18;
	}

	public Double getValue19() {
		return value19;
	}

	public void setValue19(Double value19) {
		this.value19 = value19;
	}

	public Double getValue20() {
		return value20;
	}

	public void setValue20(Double value20) {
		this.value20 = value20;
	}

	public Double getValue21() {
		return value21;
	}

	public void setValue21(Double value21) {
		this.value21 = value21;
	}

	public Double getValue22() {
		return value22;
	}

	public void setValue22(Double value22) {
		this.value22 = value22;
	}

	public Double getValue23() {
		return value23;
	}

	public void setValue23(Double value23) {
		this.value23 = value23;
	}

	public Double getValue24() {
		return value24;
	}

	public void setValue24(Double value24) {
		this.value24 = value24;
	}

	public Double getValue25() {
		return value25;
	}

	public void setValue25(Double value25) {
		this.value25 = value25;
	}

	public Double getValue26() {
		return value26;
	}

	public void setValue26(Double value26) {
		this.value26 = value26;
	}

	public Double getValue27() {
		return value27;
	}

	public void setValue27(Double value27) {
		this.value27 = value27;
	}

	public Double getValue28() {
		return value28;
	}

	public void setValue28(Double value28) {
		this.value28 = value28;
	}

	public Double getValue29() {
		return value29;
	}

	public void setValue29(Double value29) {
		this.value29 = value29;
	}

	public Double getValue30() {
		return value30;
	}

	public void setValue30(Double value30) {
		this.value30 = value30;
	}

	public Double getValue31() {
		return value31;
	}

	public void setValue31(Double value31) {
		this.value31 = value31;
	}

	public Double getValue32() {
		return value32;
	}

	public void setValue32(Double value32) {
		this.value32 = value32;
	}

	public Double getValue33() {
		return value33;
	}

	public void setValue33(Double value33) {
		this.value33 = value33;
	}

	public Double getValue34() {
		return value34;
	}

	public void setValue34(Double value34) {
		this.value34 = value34;
	}

	public Double getValue35() {
		return value35;
	}

	public void setValue35(Double value35) {
		this.value35 = value35;
	}

	public Double getValue36() {
		return value36;
	}

	public void setValue36(Double value36) {
		this.value36 = value36;
	}

	public Double getValue37() {
		return value37;
	}

	public void setValue37(Double value37) {
		this.value37 = value37;
	}

	public Double getValue38() {
		return value38;
	}

	public void setValue38(Double value38) {
		this.value38 = value38;
	}

	public Double getValue39() {
		return value39;
	}

	public void setValue39(Double value39) {
		this.value39 = value39;
	}

	public Double getValue40() {
		return value40;
	}

	public void setValue40(Double value40) {
		this.value40 = value40;
	}

	public Double getValue41() {
		return value41;
	}

	public void setValue41(Double value41) {
		this.value41 = value41;
	}

	public Double getValue42() {
		return value42;
	}

	public void setValue42(Double value42) {
		this.value42 = value42;
	}

	public Double getValue43() {
		return value43;
	}

	public void setValue43(Double value43) {
		this.value43 = value43;
	}

	public Double getValue44() {
		return value44;
	}

	public void setValue44(Double value44) {
		this.value44 = value44;
	}

	public Double getValue45() {
		return value45;
	}

	public void setValue45(Double value45) {
		this.value45 = value45;
	}

	public Double getValue46() {
		return value46;
	}

	public void setValue46(Double value46) {
		this.value46 = value46;
	}

	public Double getValue47() {
		return value47;
	}

	public void setValue47(Double value47) {
		this.value47 = value47;
	}

	public Double getValue48() {
		return value48;
	}

	public void setValue48(Double value48) {
		this.value48 = value48;
	}

	public Double getValue49() {
		return value49;
	}

	public void setValue49(Double value49) {
		this.value49 = value49;
	}

	public Double getValue50() {
		return value50;
	}

	public void setValue50(Double value50) {
		this.value50 = value50;
	}

	@Override
	public String toString() {
		return CommonService.toString(this);
	}

}