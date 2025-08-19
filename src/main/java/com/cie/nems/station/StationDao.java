package com.cie.nems.station;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;

@Component
public class StationDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<Station> getStations(List<Integer> channelIds) {
		String sql = "select s.station_id, s.short_name, s.psr_id, s.customer_id, s.capacity, "
				+ "s.parallel_capacity, c.calc_channel, c.power_source, c.energy_source, "
				+ "s.country_id, s.province_id, s.city_id, s.county_id "
				+ "from station s join station_calc_param c on s.station_id = c.station_id "
				+ "where c.calc_channel in (:channelIds) and (s.visual_flag is null or s.visual_flag = '0') ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		return myJdbcTemp.queryForBeanList(sql, paramMap, Station.class);
	}

}
