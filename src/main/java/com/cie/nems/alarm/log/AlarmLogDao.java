package com.cie.nems.alarm.log;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;

import com.cie.nems.alarm.rule.AlarmRuleService;
import com.cie.nems.common.jdbc.MyJdbcTemplate;
import com.cie.nems.common.service.CommonService;

@Component
public class AlarmLogDao {

	@Autowired
	private JdbcTemplate jdbcTemp;
	
	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<AlarmLogs> getRealAlarms(List<Integer> channelIds) {
		String sql = "select l.log_id, l.point_id, l.rule_id, l.start_time, l.end_time, "
				+ "l.station_id, l.device_id, l.alarm_level, l.alarm_type, l.alarm_action, l.alarm_text "
				+ "from alarm_logs l "
				+ "join station_calc_param s on l.station_id = s.station_id "
				+ "where s.calc_channel in (:channelIds) "
				+ "and l.partition_id = 0 "
				+ "and l.end_time is null";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, AlarmLogs.class);
	}

	public List<AlarmLogs> getRealAlarmByLogIds(List<String> logIds) {
		String sql = "select l.* from alarm_logs l where partition_id = 0 and log_id in (:logIds) ";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("logIds", logIds);
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, AlarmLogs.class);
	}

	public int insert(List<AlarmLogs> newAlarms) {
		if (CommonService.isEmpty(newAlarms)) return 0;

		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemp);
		jdbcInsert.withTableName("alarm_logs");
		jdbcInsert.compile();

		SqlParameterSource[] params = new SqlParameterSource[newAlarms.size()];
		for (int i=0; i<newAlarms.size(); ++i) {
			AlarmLogs log = newAlarms.get(i);
			params[i] = new MapSqlParameterSource()
				.addValue("device_id", log.getDeviceId(), Types.VARCHAR)
				.addValue("log_id", log.getLogId(), Types.VARCHAR)
				.addValue("alarm_level", log.getAlarmLevel(), Types.VARCHAR)
				.addValue("alarm_type", log.getAlarmType(), Types.VARCHAR)
				.addValue("alarm_source", log.getAlarmSource(), Types.VARCHAR)
				.addValue("alarm_action", log.getAlarmAction(), Types.VARCHAR)
				.addValue("alarm_text", log.getAlarmText(), Types.VARCHAR)
				.addValue("station_id", log.getStationId(), Types.VARCHAR)
				.addValue("area_id", log.getAreaId(), Types.VARCHAR)
				.addValue("device_id", log.getDeviceId(), Types.VARCHAR)
				.addValue("comp_id", log.getCompId(), Types.VARCHAR)
				.addValue("psr_id", log.getPsrId(), Types.VARCHAR)
				.addValue("point_id", log.getPointId(), Types.BIGINT)
				.addValue("rule_id", log.getRuleId(), Types.BIGINT)
				.addValue("alarm_status", log.getAlarmStatus(), Types.VARCHAR)
				.addValue("start_time", log.getStartTime(), Types.TIMESTAMP)
				.addValue("partition_id", log.getPartitionId(), Types.INTEGER)
				.addValue("end_time", log.getEndTime(), Types.TIMESTAMP)
				.addValue("alarm_check_status", log.getAlarmCheckStatus(), Types.VARCHAR)
				.addValue("alarm_check_time", log.getAlarmCheckTime(), Types.TIMESTAMP)
				.addValue("alarm_checker", log.getAlarmChecker(), Types.VARCHAR)
				.addValue("alarm_check_memo", log.getAlarmCheckMemo(), Types.VARCHAR)
				.addValue("defect_id", log.getDefectId(), Types.BIGINT)
				.addValue("affect_per", log.getAffectPer(), Types.VARCHAR)
				.addValue("affect_bus", log.getAffectBus(), Types.VARCHAR)
				.addValue("alarm_pic", log.getAlarmPic(), Types.VARCHAR)
				.addValue("create_time", log.getCreateTime(), Types.TIMESTAMP)
				.addValue("update_time", log.getUpdateTime(), Types.TIMESTAMP);
		}
		
		int[] r = jdbcInsert.executeBatch(params);
		int count = 0;
		for (int i : r) count += i;
		return count;
	}

	private static final String updateSql = "update alarm_logs set alarm_status = :alarmStatus, "
			+ "end_time = :endTime, partition_id = :partitionId, update_time = now() "
			+ "where partition_id = 0 and log_id = :logId";
	public int updateAlarmLogs(List<AlarmLogs> recoverAlarms) {
		SqlParameterSource[] params = new SqlParameterSource[recoverAlarms.size()];
		for (int i=0; i<recoverAlarms.size(); ++i) {
			AlarmLogs log = recoverAlarms.get(i);
			params[i] = new MapSqlParameterSource()
				.addValue("alarmStatus", AlarmRuleService.ALARM_STATUS_RECOVER, Types.VARCHAR)
				.addValue("endTime", log.getEndTime(), Types.TIMESTAMP)
				.addValue("partitionId", log.getPartitionId(), Types.INTEGER)
				.addValue("logId", log.getLogId(), Types.VARCHAR);
		}
		int[] result = myJdbcTemp.batchUpdate(updateSql, params);
		int count = 0;
		for (int i : result) {
			count += i;
		}
		return count;
	}

}
