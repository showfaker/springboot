package com.cie.nems.topology.cache.suntime;

import java.util.List;
import java.util.Map;

import com.cie.nems.suntime.SunTime;

public interface SunTimeCacheService {

	public int updateSunTimes(List<Integer> channelIds, List<String> stationIds);

	public List<SunTime> getSunTimes(String regionId);

	public Map<String, List<SunTime>> getSunTimes();

	public int getSunTimeCacheSize();

}
