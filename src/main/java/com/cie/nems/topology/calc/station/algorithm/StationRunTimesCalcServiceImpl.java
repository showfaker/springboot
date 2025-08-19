package com.cie.nems.topology.calc.station.algorithm;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.monitor.station.StationMonitorColumn;
import com.cie.nems.station.Station;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.cie.nems.topology.cache.station.StationCacheService;

@Service
public class StationRunTimesCalcServiceImpl implements StationRunTimesCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.station-calc:#{false}}")
	private boolean debug;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private KafkaService kafkaService;

	@Override
	public void calc(Date now) {
		Map<String, Map<String, Object>> deviceDatas = deviceMonitorCacheService.getDeviceMonitorReal();
		Map<String, Integer[]> stationDatas = new HashMap<String, Integer[]>();
		Device d = null;
		Date dateBegin = CommonService.trunc(now, TimeType.DAY);
		
		logger.info("find {} deviceMonitorReal datas to calc", deviceDatas.size());
		for (Entry<String, Map<String, Object>> e : deviceDatas.entrySet()) {
			d = deviceCacheService.getDeviceByDeviceId(e.getKey());
			if (d == null) continue;
			Map<String, Object> deviceData = deviceMonitorCacheService.getDeviceMonitorReal(e.getKey());
			if (deviceData == null) continue;
			Object monitorDate = deviceData.get(DeviceMonitorColumn.monitor_date.getName());
			if (monitorDate == null) {
				logger.error("null monitor_date: {}", CommonService.toString(deviceData));
				continue;
			}
			if (!((Long) monitorDate).equals(dateBegin.getTime())) continue;
			
			Object dst = deviceData.get(DeviceMonitorColumn.run_start_time.getName());
			Integer dStartTime = dst == null ? null : (Integer) dst;
			if (dStartTime != null && dStartTime == 0) dStartTime = null;
			Object det = (Integer) deviceData.get(DeviceMonitorColumn.run_end_time.getName());
			Integer dEndTime = det == null ? null : (Integer) det;
			if (dEndTime != null && dEndTime == 0) dEndTime = null;
			
			Integer[] stationData = stationDatas.get(d.getStationId());
			if (stationData == null) {
				stationData = new Integer[2];
				stationDatas.put(d.getStationId(), stationData);
			}
			if (dStartTime != null) {
				if (stationData[0] == null || stationData[0] > dStartTime) {
					stationData[0] = dStartTime;
				}
			}
			if (dEndTime != null) {
				if (stationData[1] == null || stationData[1] < dEndTime) {
					stationData[1] = dEndTime;
				}
			}
		}
		logger.info("get {} station runTime datas", stationDatas.size());
		
		if (stationDatas.isEmpty()) return;
		
		int count = 0;
		Station s = null;
		for (Entry<String, Integer[]> e : stationDatas.entrySet()) {
			s = stationCacheService.getStationByStationId(e.getKey());
			if (s == null) {
				logger.error("stationId {} not exists", e.getKey());
				continue;
			}
			
			Map<String, Object> data = stationMonitorCacheService.createStationMonitorData(dateBegin.getTime(), s);
			data.put(StationMonitorColumn.run_start_time.getName(), e.getValue()[0]);
			data.put(StationMonitorColumn.run_end_time.getName(), e.getValue()[1]);
			if (e.getValue()[0] != null && e.getValue()[1] != null) {
				int runTimes = e.getValue()[1] - e.getValue()[0];
				data.put(StationMonitorColumn.run_times.getName(), runTimes);
			}
			
			String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(d.getCalcChannel());
			try {
				kafkaService.sendMonitor(monitorCenterTopic, data, debug);
				++count;
			} catch (Exception ex) {
				logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(data), ex);
			}
		}
		logger.info("send {} data to monitorCenter", count);
	}

}
