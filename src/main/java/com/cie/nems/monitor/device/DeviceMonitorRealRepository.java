package com.cie.nems.monitor.device;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceMonitorRealRepository extends JpaRepository<DeviceMonitorReal, String> {

	public List<DeviceMonitorReal> findByDeviceIdInAndMonitorDate(List<String> deviceIds, Date monitorDate);

	public DeviceMonitorReal findByDeviceIdAndMonitorDate(String deviceId, Date monitorDate);

}
