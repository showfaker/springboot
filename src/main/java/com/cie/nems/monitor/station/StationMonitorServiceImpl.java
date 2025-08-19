package com.cie.nems.monitor.station;

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
public class StationMonitorServiceImpl implements StationMonitorService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.monitor-center:#{false}}")
	private boolean debug;

	@Autowired
	private StationMonitorDao stationMonitorDao;

	@Autowired
	private CalcTopoService calcTopoService;

	@Override
	public List<StationMonitorReal> getStationMonitorReals(List<Integer> channelIds) {
		return stationMonitorDao.getStationMonitorReals(channelIds);
	}
	
	@Override
	public List<Map<String, Object>> getStationMonitorRealMap(List<Integer> channelIds) {
		return stationMonitorDao.getStationMonitorRealMap(channelIds);
	}

	@Transactional
	@Override
	public void save(List<Map<String, Object>> datas) {
		int deletes = stationMonitorDao.deleteByMap(datas);
		int inserts = stationMonitorDao.insertByMap(datas);
		if (debug) {
			logger.debug("total: {}, delete {} rows, insert {} rows for station_monitor_real", 
					datas.size(), deletes, inserts);
		}
		/*
		int updates = 0, inserts = 0;
		
		int[] result = stationMonitorDao.updateByMap(datas);
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
			inserts = stationMonitorDao.insertByMap(notExists);
		}
		
		logger.debug("total: {}, update {} rows, insert {} rows for station_monitor_real", 
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
		
		int rows = stationMonitorDao.moveRealToHis(channelIds, monitorDate);
		logger.info("move {} rows from station_monitor_real to station_monitor_his", rows);
		rows = stationMonitorDao.deleteStationMonitorReals(channelIds, monitorDate);
		logger.info("delete {} rows from station_monitor_real", rows);
		Date reportDate = new Date(monitorDate.getTime() - 24*60*60*1000L);
		rows = stationMonitorDao.copyJobRecordToHis(channelIds, reportDate);
		logger.info("create {} rows from job_record to station_monitor_his", rows);
	}
}
