package com.cie.nems.monitor.station;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StationMonitorRealRepository extends JpaRepository<StationMonitorReal, String> {

	public List<StationMonitorReal> findByStationIdInAndMonitorDate(List<String> stationIds, Date monitorDate);

	public StationMonitorReal findByStationIdAndMonitorDate(String stationId, Date monitorDate);

}
