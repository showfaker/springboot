package com.cie.nems.topology.cache.monitor.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.device.DeviceService;
import com.cie.nems.monitor.MonitorCenterDao;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.monitor.device.DeviceMonitorService;

@Service
public class DeviceMonitorCacheServiceImpl implements DeviceMonitorCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.monitor-center:#{false}}")
	private boolean debug;

	@Autowired
	private DeviceMonitorService deviceMonitorService;
	
	/**
	 * Map(deviceId, DeviceMonitorReal)
	 */
	private Map<String, Map<String, Object>> realDatas = new ConcurrentHashMap<String, Map<String, Object>>();
	
	@Override
	public int initDeviceMonitorReal(List<Integer> channelIds) {
		if (CommonService.isEmpty(channelIds)) return 0;
		
		List<Map<String, Object>> datas = deviceMonitorService.getDeviceMonitorRealMap(channelIds);
		
		for (Map<String, Object> d : datas) {
			realDatas.put((String)d.get(DeviceMonitorColumn.device_id.getName()), d);
		}
		
		return realDatas.size();
	}
	
	@Override
	public Map<String, Object> getDeviceMonitorReal(String deviceId) {
		return realDatas.get(deviceId);
	}
	
	@Override
	public List<Map<String, Object>> getDeviceMonitorReals(List<String> deviceIds) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (String id : deviceIds) {
			if (realDatas.containsKey(id)) {
				list.add(realDatas.get(id));
			}
		}
		return list;
	}
	
	@Override
	public Map<String, Map<String, Object>> getDeviceMonitorReal() {
		return realDatas;
	}

	@Override
	public void save() {
		logger.info("find {} datas in cache to save", realDatas.size());
		int count = 0;
		List<Map<String, Object>> toSaves = new ArrayList<Map<String, Object>>(realDatas.size());
		for (Map<String, Object> data : realDatas.values()) {
			Object lastSaveTime = data.get(MonitorCenterDao.LAST_SAVE_TIME);
			Object updateTime = (Long) data.get(DeviceMonitorColumn.update_time.getName());
			if (updateTime == null) {
				logger.error("null updateTime: {}", CommonService.toString(data));
				updateTime = System.currentTimeMillis();
			}
			if (lastSaveTime == null || ((Long) updateTime).compareTo((Long) lastSaveTime) > 0) {
				//未保存过，或上次保存后数据有更新的才需要保存
				data.put(MonitorCenterDao.LAST_SAVE_TIME, updateTime);
				toSaves.add(data);
				
				++count;
				if (toSaves.size() >= 100) {
					deviceMonitorService.save(toSaves);
					toSaves.clear();
				}
			}
		}
		if (toSaves.size() > 0) {
			deviceMonitorService.save(toSaves);
		}
		logger.info("cache.size(): {}, save {} datas", realDatas.size(), count);
	}

	@Override
	public void update(String deviceId, Long monitorDate, Map<String, Object> data, Long now) {
		Map<String, Object> cache = realDatas.get(deviceId);
		boolean isNew = true;
		if (cache != null) {
			Object cacheMonitorDate = cache.get(DeviceMonitorColumn.monitor_date.getName());
			if (cacheMonitorDate == null) {
				//异常数据，直接覆盖
				isNew = true;
			} else {
				int compare = ((Long) cacheMonitorDate).compareTo((Long) data.get(DeviceMonitorColumn.monitor_date.getName()));
				if (compare < 0) {
					//缓存数据日期小于新数据日期，说莫缓存数据过期，可以丢弃
					isNew = true;
				} else if (compare == 0) {
					//缓存数据日期等于新数据日期，正常更新
					isNew = false;
				} else {
					//缓存数据日期大于新数据日期，新数据过期，不更新
					return;
				}
			}
		}
		
		if (isNew) {
			data.put(DeviceMonitorColumn.monitor_id.getName(), deviceId+"_"+monitorDate);
			data.put(DeviceMonitorColumn.update_time.getName(), now);
			
			for (String key : DeviceMonitorService.DEVICE_COLUMN_TYPES.keySet()) {
				if (!data.containsKey(key)) {
					data.put(key, null);
				}
			}
			setRunStatus(data, DeviceService.DEVICE_RUN_STATUS_RUNNING);
			
			realDatas.put(deviceId, data);
			if (debug) {
				logger.debug("create data: {}", CommonService.toString(data));
			}
		} else {
			cache.putAll(data);
			cache.put(DeviceMonitorColumn.update_time.getName(), now);
			if (debug) {
				logger.debug("update data: {}", CommonService.toString(data));
			}
		}
	}

	@Override
	public void setRunStatus(Map<String, Object> data, String runStatus) {
		data.put(DeviceMonitorColumn.run_status.getName(), runStatus);
		if (!DeviceService.DEVICE_RUN_STATUS_RUNNING.equals(data.get(DeviceMonitorColumn.day_run_status.getName()))
		 && DeviceService.DEVICE_RUN_STATUS_RUNNING.equals(runStatus)) {
			data.put(DeviceMonitorColumn.day_run_status.getName(), DeviceService.DEVICE_RUN_STATUS_RUNNING);
		}
	}

	@Override
	public Map<String, Object> createDeviceMonitorData(Long monitorDate, Device device) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(DeviceMonitorColumn.monitor_date.getName(), monitorDate);
		data.put(DeviceMonitorColumn.device_id.getName(), device.getDeviceId());
		data.put(DeviceMonitorColumn.station_id.getName(), device.getStationId());
		data.put(DeviceMonitorColumn.customer_id.getName(), device.getCustomerId());
		return data;
	}

	@Override
	public int getDeviceMonitorCacheSize() {
		return realDatas.size();
	}

	@Override
	public Set<String> getDeviceIds() {
		return realDatas.keySet();
	}
}
