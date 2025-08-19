package com.cie.nems.suntime;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SunTimeServiceImpl implements SunTimeService {

	@Autowired
	private SunTimeDao sunTimeDao;
	
	@Override
	public List<SunTime> getSunTimes(List<Integer> channelIds, List<String> stationIds) {
		return sunTimeDao.getSunTimes(channelIds, stationIds);
	}

}
