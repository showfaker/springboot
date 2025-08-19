package com.cie.nems.topology.cache.station;

import java.util.List;
import java.util.Map;

import com.cie.nems.station.Station;

public interface StationCacheService {

	public int initCalcCache(List<Integer> channelIds);

	public int updateStations(List<Station> stationList);

	public int deleteStations(List<Station> stationList);

	public Station getStationByStationId(String stationId);

	public Station getStationByPsrId(String psrId);

	public Map<String, Station> getStations();

	public Map<String, Station> getStationList(List<String> stationIds);

	public Map<String, Integer> getStationCacheSize();

}
