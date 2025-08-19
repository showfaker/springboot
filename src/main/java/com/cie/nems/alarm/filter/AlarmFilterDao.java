package com.cie.nems.alarm.filter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;

@Component
public class AlarmFilterDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<AlarmFilter> getAlarmFilters(List<Integer> channelIds) {
		String sql = "select f.* from alarm_filter f "
				+ "join station_calc_param s on f.station_id = s.station_id "
				+ "where s.calc_channel in (:channelIds) "
				+ "and f.end_time > now()";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, AlarmFilter.class);
	}

}
