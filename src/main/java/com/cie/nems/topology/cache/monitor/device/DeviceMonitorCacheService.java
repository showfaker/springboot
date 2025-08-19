package com.cie.nems.topology.cache.monitor.device;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cie.nems.device.Device;

public interface DeviceMonitorCacheService {

	public int initDeviceMonitorReal(List<Integer> channelIds);

	public Map<String, Object> getDeviceMonitorReal(String deviceId);

	public List<Map<String, Object>> getDeviceMonitorReals(List<String> deviceIds);

	public Map<String, Map<String, Object>> getDeviceMonitorReal();

	public void save();

	public void update(String deviceId, Long monitorDate, Map<String, Object> data, Long now);

	public Map<String, Object> createDeviceMonitorData(Long monitorDate, Device device);

	public int getDeviceMonitorCacheSize();

	public Set<String> getDeviceIds();

	public void setRunStatus(Map<String, Object> data, String runStatus);

}
