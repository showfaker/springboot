package com.cie.nems.suntime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;
import com.cie.nems.common.service.CommonService;

@Component
public class SunTimeDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<SunTime> getSunTimes(List<Integer> channelIds, List<String> stationIds) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		String subSql = "";
		if (CommonService.isNotEmpty(channelIds)) {
			subSql = " and c.calc_channel in (:channelIds) ";
			paramMap.put("channelIds", channelIds);
		}
		if (CommonService.isNotEmpty(stationIds)) {
			subSql = " and s.station_id in (:stationIds) ";
			paramMap.put("stationIds", stationIds);
		}
		String sql = "select t.* from sun_time t where t.region_id in ( "
				+ "select province_id from station s join station_calc_param c on s.station_id = c.station_id "
				+ "where s.province_id is not null "
				+ subSql
				+ "union all "
				+ "select city_id from station s join station_calc_param c on s.station_id = c.station_id "
				+ "where s.city_id is not null "
				+ subSql
				+ "union all "
				+ "select county_id from station s join station_calc_param c on s.station_id = c.station_id "
				+ "where s.county_id is not null "
				+ subSql
				+ ")";
		return myJdbcTemp.queryForBeanList(sql, paramMap, SunTime.class);
	}

}
