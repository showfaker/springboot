package com.cie.nems.schedule.job;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.test.TestService;

@Component
public class TestJob {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TestService service;

	private boolean isRunning;

	public void execute() {
		if (isRunning) return;
		
		try {
			logger.info("job starting");
			
			isRunning = true;
			
			long t = System.currentTimeMillis();
			
			service.test();
			
			logger.info("job finished! cost "+(System.currentTimeMillis() - t)+" ms");
		} catch (Exception e) {
			logger.error("do {} failed!", CommonService.getClassName(this), e);
		} finally {
			isRunning = false;
		}
	}

}
