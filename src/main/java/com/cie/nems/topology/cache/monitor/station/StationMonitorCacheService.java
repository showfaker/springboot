package com.cie.nems.topology.cache.monitor.station;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cie.nems.station.Station;

public interface StationMonitorCacheService {

	public int initStationMonitorReal(List<Integer> channelIds);

	public Map<String, Object> getStationMonitorReal(String stationId);

	public List<Map<String, Object>> getStationMonitorReals(List<String> stationIds);

	public Map<String, Map<String, Object>> getStationMonitorReal();

	public void save();

	public void update(String stationId, Long monitorDate, Map<String, Object> data, Long now);

	public Map<String, Object> createStationMonitorData(Long monitorDate, Station station);

	public int getStationMonitorCacheSize();

	public Set<String> getStationIds();

}
