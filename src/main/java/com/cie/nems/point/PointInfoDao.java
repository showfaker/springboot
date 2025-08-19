package com.cie.nems.point;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.Constants;
import com.cie.nems.common.jdbc.MyJdbcTemplate;

@Component
public class PointInfoDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<PointInfoDto> getDevicePoints(List<String> psrIds, boolean getAll) {
		String sql = "select p.point_id, p.point_name, p.obj_id as psr_id, d.device_id, d.station_id, "
				+ "d.customer_id, s.calc_channel, c.remotion_type, p.cate_id, c.sys_cate_id, "
				+ "c.calc_period, c.data_period, c.save_precision, c.dbs, c.data_type, p.calc_formula "
				+ "from measure_point p join point_category c on p.cate_id = c.cate_id "
				+ "join device d on d.psr_id = p.obj_id "
				+ "join station_calc_param s on d.station_id = s.station_id "
				+ "where p.obj_type = :objType and p.obj_id in (:psrIds) ";
		
		if (!getAll) {
			sql += "and p.cate_id not like 'A-%' and p.cate_id not like 'AI-%' ";
		}
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objType", Constants.OBJ_TYPE_PSR);
		paramMap.put("psrIds", psrIds);
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, PointInfoDto.class);
	}

	public List<PointInfoDto> getStationPoints(List<Integer> channelIds) {
		String sql = "select p.point_id, p.point_name, p.obj_id as psr_id, s.station_id, "
				+ "s.customer_id, sc.calc_channel, c.remotion_type, p.cate_id, p.calc_formula, c.sys_cate_id, "
				+ "c.calc_period, c.data_period, c.save_precision, c.dbs, c.data_type "
				+ "from measure_point p join point_category c on p.cate_id = c.cate_id "
				+ "join station s on p.obj_type = :objType and s.psr_id = p.obj_id "
				+ "join station_calc_param sc on s.station_id = sc.station_id "
				+ "where sc.calc_channel in (:channelIds) ";
		
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("objType", Constants.OBJ_TYPE_PSR);
		paramMap.put("channelIds", channelIds);
		
		return myJdbcTemp.queryForBeanList(sql, paramMap, PointInfoDto.class);
	}

}
