package com.cie.nems.topology.cache;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.NemsException;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.topology.CalcTopoService;
import com.cie.nems.topology.cache.alarm.log.AlarmLogCacheService;
import com.cie.nems.topology.cache.alarm.rule.AlarmRuleCacheService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.pre.PreRuleCacheService;
import com.cie.nems.topology.cache.station.StationCacheService;
import com.cie.nems.topology.cache.suntime.SunTimeCacheService;

@Service
public class CacheServiceImpl implements CacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PointCacheService pointCache;

	@Autowired
	private DeviceCacheService deviceCache;

	@Autowired
	private StationCacheService stationCache;

	@Autowired
	private PreRuleCacheService preRuleCache;

	@Autowired
	private AlarmRuleCacheService alarmRuleCache;

	@Autowired
	private AlarmLogCacheService alarmLogCache;

	@Autowired
	private CalcTopoService calcTopoService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@Autowired
	private SunTimeCacheService sunTimeCacheService;

	private boolean cacheInited = false;
	
	@Override
	public boolean isCacheInited() {
		return cacheInited;
	}

	@Override
	public void initLocalCache() throws NemsException {
		List<Integer> channelIds = calcTopoService.getChannelIds();
		
		if (calcTopoService.isDistr()) {
			/* 分发拓扑需要加载的channel比较多，但只需要测点档案资料，用于决定分发到哪个pre-topic */
			initDistrCache(channelIds);
		} else {
			/* 计算拓扑需要加载的channel少，一般只有一个，但需要加载测点档案、设备档案、电站档案、预处理规则、告警规则等众多资料 */
			initCalcCache(channelIds);
		}
		cacheInited = true;
	}

	private void initDistrCache(List<Integer> channelIds) {
		logger.info("****************************************************************");
		logger.info("start init distr cache for channel [{}] ...", StringUtils.join(channelIds, ','));
		
		int pointCount = pointCache.initDistrCache(channelIds);
		
		logger.info("init distr cache for channel [{}] success, load {} points", 
				StringUtils.join(channelIds, ','), pointCount);
		logger.info("****************************************************************");
	}

	private void initCalcCache(List<Integer> channelIds) {
		logger.info("****************************************************************");
		logger.info("start init calc cache for channel [{}] ...", StringUtils.join(channelIds, ','));
		
		int count = stationCache.initCalcCache(channelIds);
		
		count = initCalcPointAndDevices(channelIds);
		logger.info("load {} points", count);

		count = preRuleCache.updatePreRules(channelIds, null);
		logger.info("load {} preprocess rules", count);
		count = preRuleCache.updatePreRelas(channelIds, null, null);
		logger.info("load {} preprocess relas", count);
		
		count = alarmRuleCache.updateAlarmRules(channelIds, null);
		logger.info("load {} alarm rules", count);
		count = alarmRuleCache.updateAlarmRelas(channelIds, null, null);
		logger.info("load {} alarm relas", count);
		count = alarmRuleCache.updateAlarmFilters(channelIds);
		logger.info("load {} alarm filters", count);
		
		count = deviceMonitorCacheService.initDeviceMonitorReal(channelIds);
		logger.info("load {} device monitor real datas", count);
		count = stationMonitorCacheService.initStationMonitorReal(channelIds);
		logger.info("load {} station monitor real datas", count);
		
		count = alarmLogCache.initAlarmStatus(channelIds);
		logger.info("load {} real alarms", count);
		
		count = sunTimeCacheService.updateSunTimes(channelIds, null);
		logger.info("load {} sun times", count);
		
		logger.info("init calc cache for channel [{}] success", StringUtils.join(channelIds, ','));
		logger.info("****************************************************************");
	}

	/**
	 * 初始化设备信息及其测点信息，由于设备和测点数据量较大，所以要采取分批次加载的方式避免一次性占用资源过多
	 * @param channelIds
	 * @return
	 */
	private int initCalcPointAndDevices(List<Integer> channelIds) {
		CountDto count = new CountDto();
		
		long t1 = System.currentTimeMillis();
		
		List<String> deviceIds = new ArrayList<String>(CommonService.getListInitCapacity(DeviceCacheService.deviceBatchNumber));
		List<String> psrIds = new ArrayList<String>(CommonService.getListInitCapacity(DeviceCacheService.deviceBatchNumber));
		
		//第一批设备和测点
		Page<Device> devices = deviceCache.initDevices(0, channelIds, deviceIds, psrIds, count);
		pointCache.initDevicePoints(devices.getContent(), psrIds, count);		//初始化设备测点
		logger.debug("load {}/{} points, {}/{} expressions, {}/{} relas for {}/{} devices", 
				count.getPointCount(), count.getPointAll(), count.getExpCount(), count.getExpAll(), 
				count.getObjRelaCount(), count.getObjRelaAll(), count.getDeviceCount(), count.getDeviceAll());

		//分批次初始化后续批次设备信息和设备测点信息
		for (int i=1; i<devices.getTotalPages(); ++i) {
			devices = deviceCache.initDevices(i, channelIds, deviceIds, psrIds, count);
			pointCache.initDevicePoints(devices.getContent(), psrIds, count);		//初始化设备测点
			logger.debug("load {}/{} points, {}/{} expressions, {}/{} relas for {}/{} devices", 
					count.getPointCount(), count.getPointAll(), count.getExpCount(), count.getExpAll(), 
					count.getObjRelaCount(), count.getObjRelaAll(), count.getDeviceCount(), count.getDeviceAll());
		}
		
		long t2 = System.currentTimeMillis();
		logger.debug("used {} seconds", (1.0 * t2 - t1) / 1000L);
		
		//初始电站测点信息
		pointCache.initStationPoints(channelIds, count);
		
		long t3 = System.currentTimeMillis();
		logger.debug("used {} seconds", (1.0 * t3 - t2) / 1000L);
		
		return count.getPointAll();
	}

}
