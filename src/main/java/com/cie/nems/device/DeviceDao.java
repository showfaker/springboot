package com.cie.nems.device;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;
import com.cie.nems.common.service.CommonService;

@Component
public class DeviceDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public Page<String> getPsrIds(List<Integer> channelIds, Pageable pageable) {
		String sql = "select d.psr_id from device d "
				+ "join station_calc_param s on d.station_id = s.station_id "
				+ "where s.calc_channel in (:channelIds)"
				+ "and d.device_type not in (:escapceDeviceTypes)";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		paramMap.put("escapceDeviceTypes", Arrays.asList(DeviceService.DEVICE_TYPE_ZJ,
				DeviceService.DEVICE_TYPE_ZC, DeviceService.DEVICE_TYPE_BCJ));
		return myJdbcTemp.queryForPage(sql, paramMap, pageable, (rs, index) -> {
			return rs.getString("psr_id");
		});
	}

	public Page<Device> getDevices(List<Integer> channelIds, List<String> stationIds, List<String> deviceIds, 
			Pageable pageable) {
		String sql = "select d.device_id, d.device_name, d.psr_id, d.area_id, d.station_id, d.customer_id, "
				+ "d.device_type, d.use_flag, d.capacity, s.calc_channel, r.obj_id1 as parent_id "
				+ "from device d "
				+ "join station_calc_param s on d.station_id = s.station_id "
				+ "left join obj_rela r on d.device_id = r.obj_id2 and r.rela_type = '01' "
				+ "where s.calc_channel in (:channelIds) ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("channelIds", channelIds);
		
		if (CommonService.isNotEmpty(stationIds)) {
			paramMap.put("stationIds", stationIds);
			sql += "d.station_id in (:stationIds) ";
		}
		if (CommonService.isNotEmpty(deviceIds)) {
			paramMap.put("deviceIds", deviceIds);
			sql += "d.device_id in (:deviceIds) ";
		}
		
		return myJdbcTemp.queryForPage(sql, paramMap, pageable, Device.class);
	}

}
