package com.cie.nems.schedule.job;


import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.monitor.device.DeviceMonitorService;
import com.cie.nems.monitor.station.StationMonitorService;

@Component
public class MoveMonitorRealToHisJob {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private DeviceMonitorService deviceMonitorService;

	@Autowired
	private StationMonitorService stationMonitorService;

	private boolean isRunning;

	public void execute() {
		if (isRunning) return;
		
		try {
			logger.info("job starting");
			
			isRunning = true;
			
			long t = System.currentTimeMillis();
			
			Date monitorDate = CommonService.trunc(new Date(), TimeType.DAY);
			
			deviceMonitorService.moveRealToHis(monitorDate);
			
			stationMonitorService.moveRealToHis(monitorDate);
			
			logger.info("job finished! cost "+(System.currentTimeMillis() - t)+" ms");
		} catch (Exception e) {
			logger.error("do {} failed!", CommonService.getClassName(this), e);
		} finally {
			isRunning = false;
		}
	}

}
