package com.cie.nems.topology.cmd.station;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.station.Station;
import com.cie.nems.topology.cache.station.StationCacheService;
import com.cie.nems.topology.cmd.CmdDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StationCmdServiceImpl implements StationCmdService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private ExceptionService exceptionService;
	
	private ObjectMapper om = new ObjectMapper();

	@Override
	public void updateStationInfo(CmdDto cmd) {
		List<Station> stations = parseStationInfos(cmd.getInfo());
		if (CommonService.isNotEmpty(stations)) {
			stationCacheService.updateStations(stations);
		}
		logger.debug("update {} stationInfos", stations == null ? 0 : stations.size());
	}

	@Override
	public void deleteStationInfo(CmdDto cmd) {
		List<Station> stations = parseStationInfos(cmd.getInfo());
		if (CommonService.isNotEmpty(stations)) {
			stationCacheService.deleteStations(stations);
		}
		logger.debug("delete {} stationInfos", stations == null ? 0 : stations.size());
	}

	private List<Station> parseStationInfos(String info) {
		List<Station> stations = null;
		try {
			stations = om.readValue(info, new TypeReference<List<Station>>() {});
		} catch (Exception e) {
			logger.error("parse station info failed! {} : {}", info, e.getMessage());
			exceptionService.log(this.getClass().getName() + "-parseStationInfo", info, e);
		}
		return stations;
	}

}
