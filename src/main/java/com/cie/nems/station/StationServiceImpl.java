package com.cie.nems.station;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StationServiceImpl implements StationService {

	@Autowired
	private StationDao stationDao;

	@Override
	public List<Station> getStations(List<Integer> channelIds) {
		return stationDao.getStations(channelIds);
	}

}
