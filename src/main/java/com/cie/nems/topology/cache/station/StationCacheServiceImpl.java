package com.cie.nems.topology.cache.station;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.station.Station;
import com.cie.nems.station.StationService;

@Service
public class StationCacheServiceImpl implements StationCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StationService stationService;
	
	private Map<String, Station> psrStations = new ConcurrentHashMap<String, Station>();
	private Map<String, Station> stations = new ConcurrentHashMap<String, Station>();

	@Override
	public int initCalcCache(List<Integer> channelIds) {
		List<Station> stationList = stationService.getStations(channelIds);
		return updateStations(stationList);
	}

	@Override
	public int updateStations(List<Station> stationList) {
		if (CommonService.isEmpty(stationList)) return 0;
		
		Station pre = null;
		int psrPreSize = psrStations.size(), stationPreSize = stations.size();
		int psrUpdate = 0, psrAdd = 0, stationUpdate = 0, stationAdd = 0;
		for (Station s : stationList) {
			pre = psrStations.put(s.getPsrId(), s);
			if (pre == null) {
				++psrAdd;
			} else {
				++psrUpdate;
			}
			pre = stations.put(s.getStationId(), s);
			if (pre == null) {
				++stationAdd;
			} else {
				++stationUpdate;
			}
		}
		logger.info("psrStations {}->{}, update: {}, add: {}", psrPreSize, psrStations.size(), psrUpdate, psrAdd);
		logger.info("stations {}->{}, update: {}, add: {}", stationPreSize, stations.size(), stationUpdate, stationAdd);
		return stationList.size();
	}

	@Override
	public int deleteStations(List<Station> stationList) {
		if (CommonService.isEmpty(stationList)) return 0;
		
		Station pre = null;
		int psrPreSize = psrStations.size(), stationPreSize = stations.size();
		int psrDelete = 0, stationDelete = 0;
		for (Station s : stationList) {
			pre = psrStations.remove(s.getPsrId());
			if (pre != null) {
				++psrDelete;
			}
			pre = stations.remove(s.getStationId());
			if (pre != null) {
				++stationDelete;
			}
		}
		logger.info("psrStations {}->{}, delete: {}", psrPreSize, psrStations.size(), psrDelete);
		logger.info("stations {}->{}, delete: {}", stationPreSize, stations.size(), stationDelete);
		return stationList.size();
	}
	
	@Override
	public Station getStationByStationId(String stationId) {
		return stations.get(stationId);
	}

	@Override
	public Station getStationByPsrId(String psrId) {
		return psrStations.get(psrId);
	}

	@Override
	public Map<String, Station> getStations() {
		return stations;
	}

	@Override
	public Map<String, Station> getStationList(List<String> stationIds) {
		Map<String, Station> map = new HashMap<String, Station>();
		for (String sid : stationIds) {
			map.put(sid, stations.get(sid));
		}
		return map;
	}

	@Override
	public Map<String, Integer> getStationCacheSize() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("psrStations", psrStations.size());
		map.put("stations", stations.size());
		return map;
	}

}
