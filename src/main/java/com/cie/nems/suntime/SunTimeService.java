package com.cie.nems.suntime;

import java.util.List;

public interface SunTimeService {

	public List<SunTime> getSunTimes(List<Integer> channelIds, List<String> stationIds);

}
