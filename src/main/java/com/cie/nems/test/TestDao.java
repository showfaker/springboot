package com.cie.nems.test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;

@Component
public class TestDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public int deleteAlarmLogs(List<Integer> channelIds) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		return myJdbcTemp.update("delete from alarm_logs where station_id in ("
				+ "select station_id from station_calc_param where calc_channel in (:channelIds))", 
				paramMap);
	}

	public int deleteDeviceMonitorReals(List<Integer> channelIds) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		return myJdbcTemp.update("delete from device_monitor_real where station_id in ("
				+ "select station_id from station_calc_param where calc_channel in (:channelIds))", 
				paramMap);
	}

	public int deleteStationMonitorReals(List<Integer> channelIds) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		return myJdbcTemp.update("delete from station_monitor_real where station_id in ("
				+ "select station_id from station_calc_param where calc_channel in (:channelIds))", 
				paramMap);
	}

}
