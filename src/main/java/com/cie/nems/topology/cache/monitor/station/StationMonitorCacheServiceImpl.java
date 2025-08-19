package com.cie.nems.topology.cache.monitor.station;

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
import com.cie.nems.monitor.MonitorCenterDao;
import com.cie.nems.monitor.station.StationMonitorColumn;
import com.cie.nems.monitor.station.StationMonitorService;
import com.cie.nems.station.Station;

@Service
public class StationMonitorCacheServiceImpl implements StationMonitorCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.monitor-center:#{false}}")
	private boolean debug;

	@Autowired
	private StationMonitorService stationMonitorService;
	
	/**
	 * Map(stationId, StationMonitorReal)
	 */
	private Map<String, Map<String, Object>> realDatas = new ConcurrentHashMap<String, Map<String, Object>>();
	
	@Override
	public int initStationMonitorReal(List<Integer> channelIds) {
		if (CommonService.isEmpty(channelIds)) return 0;
		
		List<Map<String, Object>> datas = stationMonitorService.getStationMonitorRealMap(channelIds);
		
		for (Map<String, Object> d : datas) {
			realDatas.put((String)d.get(StationMonitorColumn.station_id.getName()), d);
		}
		
		return realDatas.size();
	}
	
	@Override
	public Map<String, Object> getStationMonitorReal(String stationId) {
		return realDatas.get(stationId);
	}
	
	@Override
	public List<Map<String, Object>> getStationMonitorReals(List<String> stationIds) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (String id : stationIds) {
			if (realDatas.containsKey(id)) {
				list.add(realDatas.get(id));
			}
		}
		return list;
	}
	
	@Override
	public Map<String, Map<String, Object>> getStationMonitorReal() {
		return realDatas;
	}

	@Override
	public void save() {
		logger.info("find {} datas in cache to save", realDatas.size());
		int count = 0;
		List<Map<String, Object>> toSaves = new ArrayList<Map<String, Object>>(realDatas.size());
		for (Map<String, Object> data : realDatas.values()) {
			Object lastSaveTime = data.get(MonitorCenterDao.LAST_SAVE_TIME);
			Object updateTime = (Long) data.get(StationMonitorColumn.update_time.getName());
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
					stationMonitorService.save(toSaves);
					toSaves.clear();
				}
			}
		}
		if (toSaves.size() > 0) {
			stationMonitorService.save(toSaves);
		}
		logger.info("cache.size(): {}, save {} datas", realDatas.size(), count);
	}

	@Override
	public void update(String stationId, Long monitorDate, Map<String, Object> data, Long now) {
		Map<String, Object> cache = realDatas.get(stationId);
		boolean isNew = true;
		if (cache != null) {
			Object cacheMonitorDate = cache.get(StationMonitorColumn.monitor_date.getName());
			if (cacheMonitorDate == null) {
				//异常数据，直接覆盖
				isNew = true;
			} else {
				int compare = ((Long) cacheMonitorDate).compareTo((Long) data.get(StationMonitorColumn.monitor_date.getName()));
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
			data.put(StationMonitorColumn.monitor_id.getName(), stationId+"_"+monitorDate);
			data.put(StationMonitorColumn.update_time.getName(), now);
			
			for (String key : StationMonitorService.STATION_COLUMN_TYPES.keySet()) {
				if (!data.containsKey(key)) {
					data.put(key, null);
				}
			}
			
			realDatas.put(stationId, data);
			if (debug) {
				logger.debug("create data: {}", CommonService.toString(cache));
			}
		} else {
			cache.putAll(data);
			cache.put(StationMonitorColumn.update_time.getName(), now);
			if (debug) {
				logger.debug("update data: {}", CommonService.toString(data));
			}
		}
	}

	@Override
	public Map<String, Object> createStationMonitorData(Long monitorDate, Station station) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(StationMonitorColumn.monitor_date.getName(), monitorDate);
		data.put(StationMonitorColumn.station_id.getName(), station.getStationId());
		data.put(StationMonitorColumn.customer_id.getName(), station.getCustomerId());
		data.put(StationMonitorColumn.capacity.getName(), 
				station.getParallelCapacity() == null ? station.getCapacity() : station.getParallelCapacity());
		return data;
	}

	@Override
	public int getStationMonitorCacheSize() {
		return realDatas.size();
	}

	@Override
	public Set<String> getStationIds() {
		return realDatas.keySet();
	}
}
