package com.cie.nems.topology.monitor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.monitor.station.StationMonitorColumn;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class MonitorCenterServiceImpl implements MonitorCenterService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.monitor-center:#{false}}")
	private boolean debug;

	@Autowired
	private ExceptionService exceptionService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	private ObjectMapper om = new ObjectMapper();

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) {
		List<Map<String, Object>> datas = parseMessage(msgs);

		if (CommonService.isEmpty(datas)) return;
		
		update(datas);
	}

	private List<Map<String, Object>> parseMessage(List<ConsumerRecord<Integer, String>> msgs) {
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>(msgs.size());
		for (ConsumerRecord<Integer, String> msg : msgs) {
			try {
				Map<String, Object> data = om.readValue(msg.value(), 
						new TypeReference<Map<String, Object>>() {});
				datas.add(data);
			} catch (Exception e) {
				logger.error("parse msg failed! {} : {}", msg.value(), e.getMessage());
				exceptionService.log(this.getClass().getName() + "-parseMessage", "msg", e);
			}
		}
		return datas;
	}

	private void update(List<Map<String, Object>> datas) {
		Long monitorDate = null;
		String objId = null;
		Long todayBegin = CommonService.trunc(new Date(), TimeType.DAY).getTime();
		Long now = System.currentTimeMillis();
		for (Map<String, Object> data : datas) {
			monitorDate = (Long) data.get(DeviceMonitorColumn.monitor_date.getName());
			if (monitorDate < todayBegin) continue;
			
			if (data.containsKey(DeviceMonitorColumn.device_id.getName())) {
				objId = (String) data.get(DeviceMonitorColumn.device_id.getName());
				deviceMonitorCacheService.update(objId, monitorDate, data, now);
			} else {
				objId = (String) data.get(StationMonitorColumn.station_id.getName());
				stationMonitorCacheService.update(objId, monitorDate, data, now);
			}
		}
	}

}
