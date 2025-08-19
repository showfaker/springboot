package com.cie.nems.topology.cache.dataTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.CacheConstants;
import com.cie.nems.common.redis.RedisService;
import com.cie.nems.common.redis.RedisService.Data;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;

@Service
public class DataTimeCacheServiceImpl implements DataTimeCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.pre:#{false}}")
	private boolean debug;

	@Autowired
	private RedisService redisService;

	@Value("${cie.pre.data-time-update-interval-ms:#{60000}}")
	private long updateInterval;
	
	private Map<String, Long> deviceUpdateTime = new ConcurrentHashMap<String, Long>();
	private Map<String, Long> stationUpdateTime = new ConcurrentHashMap<String, Long>();
	private Map<String, Long> deviceDataTime = new ConcurrentHashMap<String, Long>();
	private Map<String, Long> stationDataTime = new ConcurrentHashMap<String, Long>();

	@Override
	public Long getDeviceUpdateTime(String deviceId) {
		return deviceUpdateTime.get(deviceId);
	}

	@Override
	public Long getDeviceDataTime(String deviceId) {
		return deviceDataTime.get(deviceId);
	}
	
	@Override
	public void updateDeviceUpdateTime(Integer channel, Map<String, Long> times) {
		updateTimes(channel, CacheConstants.CACHE_KEY_DEVICE_UPDATE_TIME, deviceUpdateTime, times);
	}
	@Override
	public void updateDeviceDataTime(Integer channel, Map<String, Long> times) {
		updateTimes(channel, CacheConstants.CACHE_KEY_DEVICE_DATA_TIME, deviceDataTime, times);
	}
	
	@Override
	public void updateStationUpdateTime(Integer channel, Map<String, Long> times) {
		updateTimes(channel, CacheConstants.CACHE_KEY_STATION_UPDATE_TIME, stationUpdateTime, times);
	}
	@Override
	public void updateStationDataTime(Integer channel, Map<String, Long> times) {
		updateTimes(channel, CacheConstants.CACHE_KEY_STATION_DATA_TIME, stationDataTime, times);
	}
	
	/**
	 * @return 当key为NEMS_DEVICE_UPDATE_TIME时，返回数据时间有更新的设备的deviceId列表
	 */
	private void updateTimes(Integer channel, String key, Map<String, Long> cacheTimes, 
			Map<String, Long> newTimes) {
		if (CommonService.isEmpty(newTimes)) return;
		
		Map<String, String> map = new HashMap<String, String>();
		for (Entry<String, Long> e : newTimes.entrySet()) {
			Long lastTime = cacheTimes.get(e.getKey());
			if (lastTime == null || e.getValue() - lastTime > updateInterval) {
				cacheTimes.put(e.getKey(), e.getValue());
				map.put(e.getKey(), String.valueOf(e.getValue()));
			}
		}
		if (map.size() > 0) {
			redisService.hmset(Data.DATA_TIME, channel, key, map);
			if (debug) {
				logger.debug("update {} : {}", key, CommonService.toString(map));
			}
		}
	}
	@Override
	public void initDeviceUpdateTime(List<Device> devices) {
		if (CommonService.isEmpty(devices)) return;
		
		Map<Integer, List<String>> channelDeviceIds = new HashMap<Integer, List<String>>();
		for (Device d : devices) {
			List<String> deviceIds = channelDeviceIds.get(d.getCalcChannel());
			if (deviceIds == null) {
				deviceIds = new ArrayList<String>(devices.size());
				channelDeviceIds.put(d.getCalcChannel(), deviceIds);
			}
			deviceIds.add(d.getDeviceId());
		}
		
		int count = 0;
		for (Entry<Integer, List<String>> e : channelDeviceIds.entrySet()) {
			List<String> times = redisService.hmget(Data.DATA_TIME, e.getKey(), 
					CacheConstants.CACHE_KEY_DEVICE_UPDATE_TIME, e.getValue());
			for (int i=0; i<times.size(); ++i) {
				if (times.get(i) == null) continue;
				try {
					Long time = Long.valueOf(times.get(i));
					deviceUpdateTime.put(e.getValue().get(i), time);
					++count;
				} catch (NumberFormatException e1) {
					logger.error("convert device update time {} for {} failed!", times.get(i), e.getValue().get(i));
				}
			}
		}
		logger.debug("init {}/{} device update times", count, deviceUpdateTime.size());
	}

}
