package com.cie.nems.topology.cache.dataTime;

import java.util.List;
import java.util.Map;

import com.cie.nems.device.Device;

public interface DataTimeCacheService {

	public Long getDeviceUpdateTime(String deviceId);
	public Long getDeviceDataTime(String deviceId);

	public void updateDeviceUpdateTime(Integer channel, Map<String, Long> times);
	public void updateDeviceDataTime(Integer channel, Map<String, Long> times);

	public void updateStationUpdateTime(Integer channel, Map<String, Long> times);
	public void updateStationDataTime(Integer channel, Map<String, Long> times);
	
	public void initDeviceUpdateTime(List<Device> devices);

}
