package com.cie.nems.monitor.device;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.monitor.MonitorCenterDao;

@Component
public class DeviceMonitorDao {

	@Autowired
	private MonitorCenterDao monitorCenterDao;
	
	@Autowired
	private JdbcTemplate jdbcTemp;
	
	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<DeviceMonitorReal> getDeviceMonitorReals(List<Integer> channelIds) {
		String sql = "select r.* from device_monitor_real r "
				+ "join station_calc_param s on r.station_id = s.station_id "
				+ "and s.calc_channel in (:channelIds) "
				+ "where r.monitor_date = :monitorDate";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", CommonService.trunc(new Date(), TimeType.DAY));
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, DeviceMonitorReal.class);
	}

	public List<Map<String, Object>> getDeviceMonitorRealMap(List<Integer> channelIds) {
		String sql = "select r.* from device_monitor_real r "
				+ "join station_calc_param s on r.station_id = s.station_id "
				+ "and s.calc_channel in (:channelIds) "
				+ "where r.monitor_date = :monitorDate";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", CommonService.trunc(new Date(), TimeType.DAY));
		
		return myJdbcTemp.query(sql, paramMap, (rs, index)->{
			Map<String, Object> data = new HashMap<String, Object>();
			for (Entry<String, Integer> e : DeviceMonitorService.DEVICE_COLUMN_TYPES.entrySet()) {
				monitorCenterDao.setMapValue(data, e.getKey(), e.getValue(), rs);
			}
			return data;
		});
	}

	private static final String moveToHisSql = "insert into device_monitor_his "
			+ "select * from device_monitor_real r where r.station_id in ( "
			+ "select station_id from station_calc_param where calc_channel in (:channelIds)"
			+ ") and monitor_date < :monitorDate "
			+ "and not exists (select 1 from device_monitor_his where monitor_id = r.monitor_id)";
	public int moveRealToHis(List<Integer> channelIds, Date monitorDate) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", monitorDate);
		return myJdbcTemp.update(moveToHisSql, paramMap);
	}

	private static final String deleteSql = "delete from device_monitor_real r where r.station_id in ( "
			+ "select station_id from station_calc_param where calc_channel in (:channelIds)"
			+ ") and monitor_date < :monitorDate";
	public int deleteDeviceMonitorReals(List<Integer> channelIds, Date monitorDate) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", monitorDate);
		return myJdbcTemp.update(deleteSql, paramMap);
	}

	private static final String updateSql = "update device_monitor_real set real_alarms = :real_alarms, "
			+ "real_alarms_time = :real_alarms_time, "
			+ "last_alarm_time = :last_alarm_time, "
			+ "uncheck_alarms = :uncheck_alarms, "
			+ "uncheck_alarms_time = :uncheck_alarms_time, "
			+ "day_alarms = :day_alarms, "
			+ "commu_status = :commu_status, "
			+ "commu_status_time = :commu_status_time, "
			+ "last_data_time = :last_data_time, "
			+ "day_offline_num = :day_offline_num, "
			+ "day_offline_dur = :day_offline_dur, "
			+ "run_status = :run_status, "
			+ "day_run_status = :day_run_status, "
			+ "run_status_time = :run_status_time, "
			+ "run_start_time = :run_start_time, "
			+ "run_end_time = :run_end_time, "
			+ "run_times = :run_times, "
			+ "day_stop_num = :day_stop_num, "
			+ "fault_stop_duration = :fault_stop_duration, "
			+ "update_time = :update_time, "
			+ "value1 = :value1, "
			+ "value2 = :value2, "
			+ "value3 = :value3, "
			+ "value4 = :value4, "
			+ "value5 = :value5, "
			+ "value6 = :value6, "
			+ "value7 = :value7, "
			+ "value8 = :value8, "
			+ "value9 = :value9, "
			+ "value10 = :value10, "
			+ "value11 = :value11, "
			+ "value12 = :value12, "
			+ "value13 = :value13, "
			+ "value14 = :value14, "
			+ "value15 = :value15, "
			+ "value16 = :value16, "
			+ "value17 = :value17, "
			+ "value18 = :value18, "
			+ "value19 = :value19, "
			+ "value20 = :value20, "
			+ "value21 = :value21, "
			+ "value22 = :value22, "
			+ "value23 = :value23, "
			+ "value24 = :value24, "
			+ "value25 = :value25, "
			+ "value26 = :value26, "
			+ "value27 = :value27, "
			+ "value28 = :value28, "
			+ "value29 = :value29, "
			+ "value30 = :value30, "
			+ "value31 = :value31, "
			+ "value32 = :value32, "
			+ "value33 = :value33, "
			+ "value34 = :value34, "
			+ "value35 = :value35, "
			+ "value36 = :value36, "
			+ "value37 = :value37, "
			+ "value38 = :value38, "
			+ "value39 = :value39, "
			+ "value40 = :value40, "
			+ "value41 = :value41, "
			+ "value42 = :value42, "
			+ "value43 = :value43, "
			+ "value44 = :value44, "
			+ "value45 = :value45, "
			+ "value46 = :value46, "
			+ "value47 = :value47, "
			+ "value48 = :value48, "
			+ "value49 = :value49, "
			+ "value50 = :value50 "
			+ "where monitor_id = :monitor_id";
	public int[] updateByMap(List<Map<String, Object>> datas) {
		return myJdbcTemp.batchUpdate(updateSql, monitorCenterDao.getSqlParameterSourcesByMap(
				datas, DeviceMonitorService.DEVICE_COLUMN_TYPES));
	}

	private static final String deleteRealSql = "delete from device_monitor_real where monitor_id in (:monitorIds)";
	public int deleteByMap(List<Map<String, Object>> datas) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<String> monitorIds = new ArrayList<String>(1000);
		paramMap.put("monitorIds", monitorIds);
		int rows = 0;
		for (Map<String, Object> data : datas) {
			monitorIds.add((String)data.get(DeviceMonitorColumn.monitor_id.getName()));
			if (monitorIds.size() >= 1000) {
				rows += myJdbcTemp.update(deleteRealSql, paramMap);
				monitorIds.clear();
			}
		}
		if (monitorIds.size() > 0) {
			rows += myJdbcTemp.update(deleteRealSql, paramMap);
			monitorIds.clear();
		}
		return rows;
	}

	public int insertByMap(List<Map<String, Object>> datas) {
		if (CommonService.isEmpty(datas)) return 0;

		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemp);
		jdbcInsert.withTableName("device_monitor_real");
		jdbcInsert.compile();

		SqlParameterSource[] params = monitorCenterDao.getSqlParameterSourcesByMap(
				datas, DeviceMonitorService.DEVICE_COLUMN_TYPES);
		
		int[] r = jdbcInsert.executeBatch(params);
		int count = 0;
		for (int i : r) count += i;
		return count;
	}

	public int[] update(List<DeviceMonitorReal> datas) {
		return myJdbcTemp.batchUpdate(updateSql, getSqlParameterSources(datas));
	}

	public int insert(List<DeviceMonitorReal> datas) {
		if (CommonService.isEmpty(datas)) return 0;

		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemp);
		jdbcInsert.withTableName("device_monitor_real");
		jdbcInsert.compile();

		SqlParameterSource[] params = getSqlParameterSources(datas);
		
		int[] r = jdbcInsert.executeBatch(params);
		int count = 0;
		for (int i : r) count += i;
		return count;
	}

	private SqlParameterSource[] getSqlParameterSources(List<DeviceMonitorReal> datas) {
		SqlParameterSource[] params = new SqlParameterSource[datas.size()];
		for (int i=0; i<datas.size(); ++i) {
			DeviceMonitorReal log = datas.get(i);
			params[i] = new MapSqlParameterSource()
					.addValue("monitor_id", log.getMonitorId(), Types.VARCHAR)
					.addValue("monitor_date", log.getMonitorDate(), Types.DATE)
					.addValue("customer_id", log.getCustomerId(), Types.VARCHAR)
					.addValue("station_id", log.getStationId(), Types.VARCHAR)
					.addValue("device_id", log.getDeviceId(), Types.VARCHAR)
					.addValue("real_alarms", log.getRealAlarms(), Types.INTEGER)
					.addValue("real_alarms_time", log.getRealAlarmsTime(), Types.TIMESTAMP)
					.addValue("last_alarm_time", log.getLastAlarmTime(), Types.TIMESTAMP)
					.addValue("uncheck_alarms", log.getUncheckAlarms(), Types.INTEGER)
					.addValue("uncheck_alarms_time", log.getUncheckAlarmsTime(), Types.TIMESTAMP)
					.addValue("day_alarms", log.getDayAlarms(), Types.INTEGER)
					.addValue("commu_status", log.getCommuStatus(), Types.VARCHAR)
					.addValue("commu_status_time", log.getCommuStatusTime(), Types.TIMESTAMP)
					.addValue("day_offline_num", log.getDayOfflineNum(), Types.INTEGER)
					.addValue("day_offline_dur", log.getDayOfflineDur(), Types.INTEGER)
					.addValue("run_status", log.getRunStatus(), Types.VARCHAR)
					.addValue("day_run_status", log.getDayRunStatus(), Types.VARCHAR)
					.addValue("run_status_time", log.getRunStatusTime(), Types.TIMESTAMP)
					.addValue("run_start_time", log.getRunStartTime(), Types.INTEGER)
					.addValue("run_end_time", log.getRunEndTime(), Types.INTEGER)
					.addValue("run_times", log.getRunTimes(), Types.INTEGER)
					.addValue("day_stop_num", log.getDayStopNum(), Types.INTEGER)
					.addValue("fault_stop_duration", log.getFaultStopDuration(), Types.INTEGER)
					.addValue("update_time", log.getUpdateTime(), Types.TIMESTAMP)
					.addValue("value1", log.getValue1(), Types.DOUBLE)
					.addValue("value2", log.getValue2(), Types.DOUBLE)
					.addValue("value3", log.getValue3(), Types.DOUBLE)
					.addValue("value4", log.getValue4(), Types.DOUBLE)
					.addValue("value5", log.getValue5(), Types.DOUBLE)
					.addValue("value6", log.getValue6(), Types.DOUBLE)
					.addValue("value7", log.getValue7(), Types.DOUBLE)
					.addValue("value8", log.getValue8(), Types.DOUBLE)
					.addValue("value9", log.getValue9(), Types.DOUBLE)
					.addValue("value10", log.getValue10(), Types.DOUBLE)
					.addValue("value11", log.getValue11(), Types.DOUBLE)
					.addValue("value12", log.getValue12(), Types.DOUBLE)
					.addValue("value13", log.getValue13(), Types.DOUBLE)
					.addValue("value14", log.getValue14(), Types.DOUBLE)
					.addValue("value15", log.getValue15(), Types.DOUBLE)
					.addValue("value16", log.getValue16(), Types.DOUBLE)
					.addValue("value17", log.getValue17(), Types.DOUBLE)
					.addValue("value18", log.getValue18(), Types.DOUBLE)
					.addValue("value19", log.getValue19(), Types.DOUBLE)
					.addValue("value20", log.getValue20(), Types.DOUBLE)
					.addValue("value21", log.getValue21(), Types.DOUBLE)
					.addValue("value22", log.getValue22(), Types.DOUBLE)
					.addValue("value23", log.getValue23(), Types.DOUBLE)
					.addValue("value24", log.getValue24(), Types.DOUBLE)
					.addValue("value25", log.getValue25(), Types.DOUBLE)
					.addValue("value26", log.getValue26(), Types.DOUBLE)
					.addValue("value27", log.getValue27(), Types.DOUBLE)
					.addValue("value28", log.getValue28(), Types.DOUBLE)
					.addValue("value29", log.getValue29(), Types.DOUBLE)
					.addValue("value30", log.getValue30(), Types.DOUBLE)
					.addValue("value31", log.getValue31(), Types.DOUBLE)
					.addValue("value32", log.getValue32(), Types.DOUBLE)
					.addValue("value33", log.getValue33(), Types.DOUBLE)
					.addValue("value34", log.getValue34(), Types.DOUBLE)
					.addValue("value35", log.getValue35(), Types.DOUBLE)
					.addValue("value36", log.getValue36(), Types.DOUBLE)
					.addValue("value37", log.getValue37(), Types.DOUBLE)
					.addValue("value38", log.getValue38(), Types.DOUBLE)
					.addValue("value39", log.getValue39(), Types.DOUBLE)
					.addValue("value40", log.getValue40(), Types.DOUBLE)
					.addValue("value41", log.getValue41(), Types.DOUBLE)
					.addValue("value42", log.getValue42(), Types.DOUBLE)
					.addValue("value43", log.getValue43(), Types.DOUBLE)
					.addValue("value44", log.getValue44(), Types.DOUBLE)
					.addValue("value45", log.getValue45(), Types.DOUBLE)
					.addValue("value46", log.getValue46(), Types.DOUBLE)
					.addValue("value47", log.getValue47(), Types.DOUBLE)
					.addValue("value48", log.getValue48(), Types.DOUBLE)
					.addValue("value49", log.getValue49(), Types.DOUBLE)
					.addValue("value50", log.getValue50(), Types.DOUBLE);
		}
		return params;
	}

}
