package com.cie.nems.device;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DeviceServiceImpl implements DeviceService {

	@Autowired
	private DeviceDao deviceDao;

	@Override
	public Page<String> getPsrIds(List<Integer> channelIds, Pageable pageable) {
		return deviceDao.getPsrIds(channelIds, pageable);
	}

	@Override
	public Page<Device> getDevices(List<Integer> channelIds, List<String> stationIds, List<String> deviceIds, 
			Pageable pageable) {
		return deviceDao.getDevices(channelIds, stationIds, deviceIds, pageable);
	}

}
