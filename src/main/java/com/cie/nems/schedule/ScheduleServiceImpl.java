package com.cie.nems.schedule;

import java.lang.reflect.Method;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.TriggerContext;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.util.SpringContextUtil;
import com.cie.nems.topology.cache.CacheService;

@DependsOn(value = "application")
@Service
@Configuration
@EnableScheduling
public class ScheduleServiceImpl implements SchedulingConfigurer, ScheduleService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.id}")
	private String appId;
	
	@Autowired
	private ScheduleJobRepository scheduleJobRepo;

	@Autowired
	private CommonService commonService;
	
	@Autowired
	private CacheService cacheService;
	
	@Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
		String ip = null;
		try {
			ip = commonService.getLocalHostLANAddress().getHostAddress();
		} catch (Exception e) {
			logger.error("get local host address failed!", e);
		}
		
		StringBuffer info = new StringBuffer();
		info.append("\n****************************************************************");
		info.append("\n* starting job for ip: ").append(ip).append(", app.id: ").append(appId);
		List<ScheduleJob> jobs = scheduleJobRepo.findByJobIpAndJobAppAndJobStatus(
				ip, appId, SCHEDULE_STATUS_ACTIVE);
		
		int count = 0;
		if (CommonService.isNotEmpty(jobs)) {
			for (ScheduleJob job : jobs) {
				if (StringUtils.isBlank(job.getJobClass())) {
					logger.error("job {}[id: {}] 未指定class", job.getJobName(), job.getJobId());
					continue;
				}
				if (StringUtils.isBlank(job.getJobMethod())) {
					logger.error("job {}[id: {}] 未指定methodclass", job.getJobName(), job.getJobId());
					continue;
				}
				if (StringUtils.isBlank(job.getJobCron())) {
					logger.error("job {}[id: {}] 未指定cron", job.getJobName(), job.getJobId());
					continue;
				}
				
				//首字母改为小写
				char c = job.getJobClass().charAt(0);
				if (c >= 65 && c <= 90) {
					c += 32;
					job.setJobClass(c + job.getJobClass().substring(1));
				}
				
				info.append("\n* starting job ").append(job.getJobName())
					.append(", cron: ").append(job.getJobCron())
					.append(", class: ").append(job.getJobClass())
					.append(", method: ").append(job.getJobMethod());
				
				++count;
				
				taskRegistrar.addTriggerTask(new Runnable() {
					@Override
					public void run() {
						try {
							if (cacheService.isCacheInited()) {
								//根据类名和方法名反射调用job
								Object jobCls = SpringContextUtil.getBean(job.getJobClass());
								Method objMethod = jobCls.getClass().getMethod(job.getJobMethod());
								objMethod.invoke(jobCls);
							} else {
								logger.debug("cache not inited, do not call job {}.{}", job.getJobClass(), job.getJobMethod());
							}
						} catch (Exception e) {
							logger.error("call {}(class: {}, method: {}) failed!", job.getJobName(), 
									job.getJobClass(), job.getJobMethod(), e);
						}
					}
				}, new Trigger() {
					@Override
					public Date nextExecutionTime(TriggerContext triggerContext) {
						return new CronTrigger(job.getJobCron()).nextExecutionTime(triggerContext);
					}
				});
			}
		}
		if (count == 0) {
			info.append("\n* find 0 jobs to execute");
		}
		info.append("\n****************************************************************");
		logger.info(info.toString());
	}
}
