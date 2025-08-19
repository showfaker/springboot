package com.cie.nems.monitor.device;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cie.nems.common.Constants;
import com.cie.nems.common.exception.NemsException;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.topology.CalcTopoService;

@Service
public class DeviceMonitorServiceImpl implements DeviceMonitorService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.monitor-center:#{false}}")
	private boolean debug;

	@Autowired
	private DeviceMonitorDao deviceMonitorDao;

	@Autowired
	private CalcTopoService calcTopoService;

	@Override
	public List<DeviceMonitorReal> getDeviceMonitorReals(List<Integer> channelIds) {
		return deviceMonitorDao.getDeviceMonitorReals(channelIds);
	}
	
	@Override
	public List<Map<String, Object>> getDeviceMonitorRealMap(List<Integer> channelIds) {
		return deviceMonitorDao.getDeviceMonitorRealMap(channelIds);
	}

	@Transactional
	@Override
	public void save(List<Map<String, Object>> datas) {
		int deletes = deviceMonitorDao.deleteByMap(datas);
		int inserts = deviceMonitorDao.insertByMap(datas);
		if (debug) {
			logger.debug("total: {}, delete {} rows, insert {} rows for device_monitor_real", 
					datas.size(), deletes, inserts);
		}
		/*
		int updates = 0, inserts = 0;
		
		int[] result = null;
		try {
			result = deviceMonitorDao.updateByMap(datas);
		} catch(Exception e) {
			logger.error("update device_monitor_real failed! {}", e.getMessage());
			for (Map<String, Object> data : datas) {
				logger.error(CommonService.toString(data));
			}
		}
		for (int r : result) {
			updates += r;
		}
		
		if (updates < datas.size()) {
			List<Map<String, Object>> notExists = new ArrayList<Map<String, Object>>();
			for (int i=0; i<result.length; ++i) {
				if (result[i] == 0) {
					notExists.add(datas.get(i));
				}
			}
			inserts = deviceMonitorDao.insertByMap(notExists);
		}
		
		logger.debug("total: {}, update {} rows, insert {} rows for device_monitor_real", 
				datas.size(), updates, inserts);
		*/
	}

	@Transactional
	@Override
	public void moveRealToHis(Date monitorDate) throws NemsException {
		List<Integer> channelIds = calcTopoService.getChannelIds();
		logger.info("monitorDate: {}, channelIds: {}",
				CommonService.formatDate(Constants.dateFormatMillisecond, monitorDate),
				CommonService.join(channelIds, ','));
		
		int rows = deviceMonitorDao.moveRealToHis(channelIds, monitorDate);
		logger.info("move {} rows from device_monitor_real to device_monitor_his", rows);
		rows = deviceMonitorDao.deleteDeviceMonitorReals(channelIds, monitorDate);
		logger.info("delete {} rows from device_monitor_real", rows);
	}
}
