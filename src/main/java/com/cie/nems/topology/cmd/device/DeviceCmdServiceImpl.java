package com.cie.nems.topology.cmd.device;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cmd.CmdDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DeviceCmdServiceImpl implements DeviceCmdService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private ExceptionService exceptionService;
	
	private ObjectMapper om = new ObjectMapper();

	@Override
	public void updateDeviceInfo(CmdDto cmd) {
		List<Device> devices = parseDeviceInfos(cmd.getInfo());
		if (CommonService.isNotEmpty(devices)) {
			deviceCacheService.updateDevices(devices);
		}
		logger.debug("update {} deviceInfos", devices == null ? 0 : devices.size());
	}

	@Override
	public void deleteDeviceInfo(CmdDto cmd) {
		List<Device> devices = parseDeviceInfos(cmd.getInfo());
		if (CommonService.isNotEmpty(devices)) {
			deviceCacheService.deleteDevices(devices);
		}
		logger.debug("delete {} deviceInfos", devices == null ? 0 : devices.size());
	}

	private List<Device> parseDeviceInfos(String info) {
		List<Device> devices = null;
		try {
			devices = om.readValue(info, new TypeReference<List<Device>>() {});
		} catch (Exception e) {
			logger.error("parse device info failed! {} : {}", info, e.getMessage());
			exceptionService.log(this.getClass().getName() + "-parseDeviceInfo", info, e);
		}
		return devices;
	}

}
