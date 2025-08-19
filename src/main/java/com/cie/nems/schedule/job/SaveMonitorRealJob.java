package com.cie.nems.schedule.job;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;

@Component
public class SaveMonitorRealJob {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	private boolean isRunning;

	public void execute() {
		if (isRunning) return;
		
		try {
			logger.info("job starting");
			
			isRunning = true;
			
			long t = System.currentTimeMillis();
			
			deviceMonitorCacheService.save();
			
			stationMonitorCacheService.save();
			
			logger.info("job finished! cost "+(System.currentTimeMillis() - t)+" ms");
		} catch (Exception e) {
			logger.error("do {} failed!", CommonService.getClassName(this), e);
		} finally {
			isRunning = false;
		}
	}

}
