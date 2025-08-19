package com.cie.nems.monitor.station;

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
public class StationMonitorDao {

	@Autowired
	private MonitorCenterDao monitorCenterDao;
	
	@Autowired
	private JdbcTemplate jdbcTemp;
	
	@Autowired
	private MyJdbcTemplate myJdbcTemp;

	public List<StationMonitorReal> getStationMonitorReals(List<Integer> channelIds) {
		String sql = "select r.* from station_monitor_real r "
				+ "join station_calc_param s on r.station_id = s.station_id "
				+ "and s.calc_channel in (:channelIds) "
				+ "where r.monitor_date = :monitorDate";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", CommonService.trunc(new Date(), TimeType.DAY));
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, StationMonitorReal.class);
	}

	public List<Map<String, Object>> getStationMonitorRealMap(List<Integer> channelIds) {
		String sql = "select r.* from station_monitor_real r "
				+ "join station_calc_param s on r.station_id = s.station_id "
				+ "and s.calc_channel in (:channelIds) "
				+ "where r.monitor_date = :monitorDate";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", CommonService.trunc(new Date(), TimeType.DAY));
		
		return myJdbcTemp.query(sql, paramMap, (rs, index)->{
			Map<String, Object> data = new HashMap<String, Object>();
			for (Entry<String, Integer> e : StationMonitorService.STATION_COLUMN_TYPES.entrySet()) {
				monitorCenterDao.setMapValue(data, e.getKey(), e.getValue(), rs);
			}
			return data;
		});
	}

	private static final String moveToHisSql = "insert into station_monitor_his ("
			+ 		"monitor_id, monitor_date, customer_id, station_id, "
			+ 		"health_total, health1, health2, health3, health4, health5, "
			+ 		"energy_input, energy_coll, capacity, "
			+ 		"run_start_time, run_end_time, run_times, power, max_power, max_power_time, "
			+ 		"run_status, day_run_status, update_time, commu_status) "
			+ "select r.monitor_id, r.monitor_date, r.customer_id, r.station_id, "
			+ 		"r.health_total, r.health1, r.health2, r.health3, r.health4, r.health5, "
			+ 		"coalesce(j.daily_energy, r.energy_input), r.energy_coll, r.capacity, "
			+ 		"r.run_start_time, r.run_end_time, r.run_times, r.power, r.max_power, r.max_power_time, "
			+ 		"r.run_status, r.day_run_status, r.update_time, r.commu_status "
			+ "from station_monitor_real r "
			+ "left join job_record j on r.monitor_date = j.report_date and r.station_id = j.station_id "
			+ "where r.station_id in ( "
			+ 	"select station_id from station_calc_param where calc_channel in (:channelIds)"
			+ ") and monitor_date < :monitorDate "
			+ "and not exists (select 1 from station_monitor_his where monitor_id = r.monitor_id)";
	public int moveRealToHis(List<Integer> channelIds, Date monitorDate) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", monitorDate);
		return myJdbcTemp.update(moveToHisSql, paramMap);
	}

	private static final String copyJobRecordSql = "insert into station_monitor_his ("
			+ 		"monitor_id, monitor_date, customer_id, station_id, "
			+ 		"health_total, health1, health2, health3, health4, health5, "
			+ 		"energy_input, energy_coll, capacity, "
			+ 		"run_start_time, run_end_time, run_times, power, max_power, max_power_time, "
			+ 		"run_status, day_run_status, update_time, commu_status) "
			+ "select r.station_id||'_'||:reportDate, r.report_date, r.customer_id, r.station_id, "
			+ 		"null, null, null, null, null, null, "
			+ 		"r.daily_energy, null, coalesce(s.parallel_capacity, s.capacity), "
			+ 		"null, null, null, null, null, null, "
			+ 		"'0', '0', now(), null "
			+ "from job_record r "
			+ "join station s on r.station_id = s.station_id "
			+ "where r.station_id in ( "
			+ 	"select station_id from station_calc_param where calc_channel in (:channelIds) "
			+ ") and r.report_date = :reportDate "
			+ "and not exists ("
			+ 	"select 1 from station_monitor_his where station_id = r.station_id and monitor_date = r.report_date"
			+ ")";
	public int copyJobRecordToHis(List<Integer> channelIds, Date reportDate) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("reportDate", reportDate);
		return myJdbcTemp.update(copyJobRecordSql, paramMap);
	}

	private static final String deleteSql = "delete from station_monitor_real r where r.station_id in ( "
			+ "select station_id from station_calc_param where calc_channel in (:channelIds)"
			+ ") and monitor_date < :monitorDate";
	public int deleteStationMonitorReals(List<Integer> channelIds, Date monitorDate) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("monitorDate", monitorDate);
		return myJdbcTemp.update(deleteSql, paramMap);
	}

	private static final String updateSql = "update station_monitor_real set health_total = :health_total, "
			+ "health1 = :health1, "
			+ "health2 = :health2, "
			+ "health3 = :health3, "
			+ "health4 = :health4, "
			+ "health5 = :health5, "
			+ "energy_input = :energy_input, "
			+ "energy_coll = :energy_coll, "
			+ "capacity = :capacity, "
			+ "run_start_time = :run_start_time, "
			+ "run_end_time = :run_end_time, "
			+ "run_times = :run_times, "
			+ "power = :power, "
			+ "max_power = :max_power, "
			+ "max_power_time = :max_power_time, "
			+ "run_status = :run_status, "
			+ "day_run_status = :day_run_status, "
			+ "update_time = :update_time, "
			+ "commu_status = :commu_status "
			+ "where monitor_id = :monitor_id";
	public int[] updateByMap(List<Map<String, Object>> datas) {
		return myJdbcTemp.batchUpdate(updateSql, monitorCenterDao.getSqlParameterSourcesByMap(
				datas, StationMonitorService.STATION_COLUMN_TYPES));
	}

	private static final String deleteRealSql = "delete from station_monitor_real where monitor_id in (:monitorIds)";
	public int deleteByMap(List<Map<String, Object>> datas) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		List<String> monitorIds = new ArrayList<String>(1000);
		paramMap.put("monitorIds", monitorIds);
		int rows = 0;
		for (Map<String, Object> data : datas) {
			monitorIds.add((String)data.get(StationMonitorColumn.monitor_id.getName()));
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
		jdbcInsert.withTableName("station_monitor_real");
		jdbcInsert.compile();

		SqlParameterSource[] params = monitorCenterDao.getSqlParameterSourcesByMap(
				datas, StationMonitorService.STATION_COLUMN_TYPES);
		
		int[] r = jdbcInsert.executeBatch(params);
		int count = 0;
		for (int i : r) count += i;
		return count;
	}

	public int[] update(List<StationMonitorReal> datas) {
		return myJdbcTemp.batchUpdate(updateSql, getSqlParameterSources(datas));
	}

	public int insert(List<StationMonitorReal> datas) {
		if (CommonService.isEmpty(datas)) return 0;

		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemp);
		jdbcInsert.withTableName("station_monitor_real");
		jdbcInsert.compile();

		SqlParameterSource[] params = getSqlParameterSources(datas);
		
		int[] r = jdbcInsert.executeBatch(params);
		int count = 0;
		for (int i : r) count += i;
		return count;
	}

	private SqlParameterSource[] getSqlParameterSources(List<StationMonitorReal> datas) {
		SqlParameterSource[] params = new SqlParameterSource[datas.size()];
		for (int i=0; i<datas.size(); ++i) {
			StationMonitorReal log = datas.get(i);
			params[i] = new MapSqlParameterSource()
					.addValue("monitor_id", log.getMonitorId(), Types.VARCHAR)
					.addValue("monitor_date", log.getMonitorDate(), Types.DATE)
					.addValue("customer_id", log.getCustomerId(), Types.VARCHAR)
					.addValue("station_id", log.getStationId(), Types.VARCHAR)
					.addValue("health_total", log.getHealthTotal(), Types.INTEGER)
					.addValue("health1", log.getHealth1(), Types.INTEGER)
					.addValue("health2", log.getHealth2(), Types.INTEGER)
					.addValue("health3", log.getHealth3(), Types.INTEGER)
					.addValue("health4", log.getHealth4(), Types.INTEGER)
					.addValue("health5", log.getHealth5(), Types.INTEGER)
					.addValue("energy_input", log.getEnergyInput(), Types.DOUBLE)
					.addValue("energy_coll", log.getEnergyColl(), Types.DOUBLE)
					.addValue("capacity", log.getCapacity(), Types.DOUBLE)
					.addValue("run_start_time", log.getRunStartTime(), Types.INTEGER)
					.addValue("run_end_time", log.getRunEndTime(), Types.INTEGER)
					.addValue("run_times", log.getRunTimes(), Types.INTEGER)
					.addValue("power", log.getPower(), Types.DOUBLE)
					.addValue("max_power", log.getMaxPower(), Types.DOUBLE)
					.addValue("max_power_time", log.getMaxPowerTime(), Types.TIMESTAMP)
					.addValue("run_status", log.getRunStatus(), Types.VARCHAR)
					.addValue("day_run_status", log.getDayRunStatus(), Types.VARCHAR)
					.addValue("commu_status", log.getCommuStatus(), Types.VARCHAR)
					.addValue("update_time", log.getUpdateTime(), Types.TIMESTAMP);
		}
		return params;
	}

}
