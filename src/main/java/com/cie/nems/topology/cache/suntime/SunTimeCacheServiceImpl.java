package com.cie.nems.topology.cache.suntime;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.suntime.SunTime;
import com.cie.nems.suntime.SunTimeService;

@Service
public class SunTimeCacheServiceImpl implements SunTimeCacheService {

	@Value("${cie.app.debug.alarm:#{false}}")
	private boolean debug;

	@Autowired
	private SunTimeService sunTimeService;
	
	/** Map(regionId, [SunTimes]> */
	private Map<String, List<SunTime>> suntimeMap = new ConcurrentHashMap<String, List<SunTime>>();
	
	@Override
	public int updateSunTimes(List<Integer> channelIds, List<String> stationIds) {
		List<SunTime> times = sunTimeService.getSunTimes(channelIds, stationIds);
		
		if (CommonService.isEmpty(times)) return 0;
		
		for (SunTime t : times) {
			List<SunTime> list = suntimeMap.get(t.getRegionId());
			if (list == null) {
				list = new LinkedList<SunTime>();
				suntimeMap.put(t.getRegionId(), list);
			}
			list.add(t);
		}
		
		return suntimeMap.size();
	}

	@Override
	public Map<String, List<SunTime>> getSunTimes() {
		return suntimeMap;
	}

	@Override
	public List<SunTime> getSunTimes(String regionId) {
		return suntimeMap.get(regionId);
	}

	@Override
	public int getSunTimeCacheSize() {
		return suntimeMap.size();
	}

}
