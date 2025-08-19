package com.cie.nems.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cie.nems.common.CacheConstants;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.redis.RedisService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.device.DeviceService;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.monitor.station.StationMonitorColumn;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.station.Station;
import com.cie.nems.station.StationService;
import com.cie.nems.topology.CalcTopoService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.station.StationCacheService;

@Service
public class TestServiceImpl implements TestService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.monitor-center:#{false}}")
	private boolean debug;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemplate;

	@Autowired
	private PointDataRealRepository pointDataRealRepo;

	@Autowired
	private RedisService redisService;

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private TestDao testDao;

	@Autowired
	private CalcTopoService calcTopoService;

	@Value("${distr-topic-name}")
	private String distrTopic;
	
	@Override
	public Integer kafkaSendToDistr(List<Long> poitnIds, Integer offset, Integer limit) throws Exception {
		
		redisService.delete(null, null, CacheConstants.CACHE_POINT_CURR_VALUE);
		redisService.delete(null, null, CacheConstants.NEMS_CACHE_POINT_REMAIN_TIME);
		redisService.delete(null, null, CacheConstants.CACHE_KEY_DEVICE_DATA_TIME);
		redisService.delete(null, null, CacheConstants.CACHE_KEY_DEVICE_UPDATE_TIME);
		redisService.delete(null, null, CacheConstants.CACHE_KEY_STATION_DATA_TIME);
		redisService.delete(null, null, CacheConstants.CACHE_KEY_STATION_UPDATE_TIME);
		redisService.delete(null, null, CacheConstants.CACHE_ALARM_NOTICES);
		
		List<PointDataReal> values = pointDataRealRepo.findByPidInOrderByDt(poitnIds);
		
		logger.debug("find {} data from point_data_real", values == null ? 0 : values.size());
		
		if (CommonService.isEmpty(values)) return 0;
		
		int count = 0;
		
		if (offset == null) offset = 0;
		if (limit == null) limit = values.size();
		
		if (offset >= values.size()) {
			logger.debug("offset {} is larger then values.size() {}", offset, values.size());
			return 0;
		}
		if (offset + limit > values.size()) {
			logger.debug("offset + limit is {}, but values.size() is {}, change limit to {}", 
					offset + limit, values.size(), values.size() - offset);
			limit = values.size() - offset;
		}
		limit = offset + limit;
		
		PointDataReal v = null;
		for (int i=offset; i<limit; ++i) {
			v = values.get(i);
			String msg = "[{\"dt\":" + v.getDt().getTime() + 
					",\"v\":\"" + v.getV() + 
					"\",\"pid\":\"" + v.getPid() + 
					"\",\"deviceId\":\"004c9450\"}]";
			kafkaTemplate.send(distrTopic, msg).get();
			logger.debug("send to {}: {}", distrTopic, msg);
			++count;
		}
		return count;
	}

	@Transactional
	@Override
	public void deleteDatas(List<Integer> channelIds) {
		int rows = testDao.deleteAlarmLogs(channelIds);
		logger.info("delete {} rows from alarm_logs", rows);
		
		rows = testDao.deleteDeviceMonitorReals(channelIds);
		logger.info("delete {} rows from device_monitor_real", rows);
		
		rows = testDao.deleteStationMonitorReals(channelIds);
		logger.info("delete {} rows from station_monitor_real", rows);
	}

	private static boolean firstTest = true;
	private static int dataIndex = 0;
	@Override
	public void test() throws Exception {
		if (firstTest) {
			firstTest = false;
			redisService.delete(null, null, "NEMS_CACHE_POINT_CURR_VALUE");
			redisService.delete(null, null, "NEMS_CACHE_POINT_REMAIN_TIME");
			redisService.delete(null, null, "NEMS_DEVICE_DATA_TIME");
			redisService.delete(null, null, "NEMS_DEVICE_UPDATE_TIME");
			redisService.delete(null, null, "NEMS_STATION_DATA_TIME");
			redisService.delete(null, null, "NEMS_STATION_UPDATE_TIME");
			redisService.delete(null, null, "NEMS_CACHE_ALARM_NOTICES");
			
			List<Integer> channelIds = calcTopoService.getChannelIds();
			
			deleteDatas(channelIds);
		}
		List<Long> pids = Arrays.asList(
				5582167L		//AM-NB-ZXYGGL-GRID
//				,5582516L	//AM-NB-ZXYGDD-A
//				,5582249L	//AM-NB-ZXYGDD-D
//				,5582149L	//01232_0020172217 -> AM-NB-PVI-INPUT
				);
		List<PointDataReal> values = pointDataRealRepo.findByPidInOrderByDt(pids);
		
		Calendar c = Calendar.getInstance();
		Calendar today = Calendar.getInstance();
		int year = today.get(Calendar.YEAR);
		int month = today.get(Calendar.MONTH);
		int day = today.get(Calendar.DAY_OF_MONTH);
		
		if (dataIndex >= values.size()) return;
		
		PointDataReal v = values.get(dataIndex++);
		c.setTimeInMillis(v.getDt().getTime());
		//if (c.get(Calendar.HOUR_OF_DAY) < 6) return;
		
		c.set(Calendar.YEAR, year);
		c.set(Calendar.MONTH, month);
		c.set(Calendar.DAY_OF_MONTH, day);
		
		String msg = "[{\"dt\":" + c.getTimeInMillis() + 
				",\"v\":\"" + v.getV() + 
				"\",\"pid\":\"" + v.getPid() + 
				"\",\"deviceId\":\"004c9450\"}]";
		kafkaTemplate.send(distrTopic, msg).get();
		logger.debug("send to {}: {}", distrTopic, msg);
	}

	@Override
	public void createMonitorRealData(Date date) {
		try {
			createDeviceMonitorRealData(date);
		} catch (Exception e) {
			logger.error("create device_monitor_real failed!", e);
		}
		try {
			createStationMonitorRealData(date);
		} catch (Exception e) {
			logger.error("create station_monitor_real failed!", e);
		}
	}

	private void createDeviceMonitorRealData(Date date) throws Exception {
		Map<Integer, List<Long>> channelPids = new HashMap<Integer, List<Long>>();
		int inverterPointCount = 0, meterPointCount = 0, inverterCount = 0, meterCount = 0;
		
		for (Device d : deviceCacheService.getDevices().values()) {
			List<Long> pids = channelPids.get(d.getCalcChannel());
			if (pids == null) {
				pids = new ArrayList<Long>();
				channelPids.put(d.getCalcChannel(), pids);
			}
			
			//逆变器
			if (DeviceService.DEVICE_TYPE_JNB.equals(d.getDeviceType())
			 || DeviceService.DEVICE_TYPE_NB.equals(d.getDeviceType())) {
				inverterPointCount += getPointIds(pids, d.getPsrId(), PointConstants.CATE_ID_AM_NB_POWER);	//功率
				inverterPointCount += getPointIds(pids, d.getPsrId(), PointConstants.CATE_ID_AI_NB_MAXP_D);	//日最大功率
				inverterPointCount += getPointIds(pids, d.getPsrId(), PointConstants.CATE_ID_AM_NB_ENERGY_ALL);	//累计发电量
				inverterPointCount += getPointIds(pids, d.getPsrId(), PointConstants.CATE_ID_AM_NB_ENERGY_DAY);	//日发电量
				inverterPointCount += getPointIds(pids, d.getPsrId(), PointConstants.CATE_ID_AI_NB_ZXYGDD_D);	//日发电量
				inverterPointCount += getPointIds(pids, d.getPsrId(), PointConstants.CATE_ID_AI_NB_ZXYGDXS_D);	//日等效时
				++inverterCount;
			} else if (DeviceService.DEVICE_TYPE_DNB.equals(d.getDeviceType())) {
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-ZXYGZDN");	//正向有功总电能
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-ZXWGZDN");	//正向无功总电能
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-FXYGZDN");	//反向有功总电能
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-FXWGZDN");	//反向无功总电能
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-ZXYGDNJ");	//正向有功电能（尖）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-FXYGDNJ");	//反向有功电能（尖）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-ZXYGDNF");	//正向有功电能（峰）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-FXYGDNF");	//反向有功电能（峰）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-ZXYGDNP");	//正向有功电能（平）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-FXYGDNP");	//反向有功电能（平）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-ZXYGDNG");	//正向有功电能（谷）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AM-DNB-FXYGDNG");	//反向有功电能（谷）
				meterPointCount += getPointIds(pids, d.getPsrId(), "AI-DNB-ZXYGZDN-H");	//正向有功小时电量
				meterPointCount += getPointIds(pids, d.getPsrId(), "AI-DNB-ZXYGZDN-D");	//正向有功日电量
				meterPointCount += getPointIds(pids, d.getPsrId(), "AI-DNB-ZXYGZDN-M");	//正向有功月电量
				meterPointCount += getPointIds(pids, d.getPsrId(), "AI-DNB-ZXYGZDN-Y");	//正向有功年电量
				++meterCount;
			} else {
				continue;
			}
		}
		logger.info("find {} points for {} inverters , {} points for {} meters", 
				inverterPointCount, inverterCount, meterPointCount, meterCount);
		
		Date monitorDate = CommonService.trunc(date, TimeType.DAY);
		for (Entry<Integer, List<Long>> e : channelPids.entrySet()) {
			Map<String, Map<String, Object>> datas = new HashMap<String, Map<String, Object>>();
			
			Map<Long, PointValueDto> values = pointValueCacheService.getPointCurrValuesByPointIds(e.getKey(), e.getValue());
			logger.info("get {} point values for channel {}", values.size(), e.getKey());
			
			PointInfoDto point = null;
			for (PointValueDto v : values.values()) {
				if (v == null) continue;
				point = pointCacheService.getPointByPointId(v.getPid());
				if (point == null) continue;
				pointValueCacheService.setQByTime(v, point.getDataPeriod(), monitorDate);
				if (!pointValueCacheService.isValid(v)) continue;
				
				Map<String, Object> data = datas.get(point.getDeviceId());
				if (data == null) {
					Device d = deviceCacheService.getDeviceByDeviceId(point.getDeviceId());
					if (d == null) continue;
					data = deviceMonitorCacheService.createDeviceMonitorData(monitorDate.getTime(), d);
					datas.put(point.getDeviceId(), data);
				}
				
				//过大的值无法存入数据库
				if (v.getDv() > 999999999999.0) {
					logger.error("illegal point value: {}", v.toString());
					continue;
				}
				if (PointConstants.CATE_ID_AM_NB_POWER.equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value11.getName(), v.getDv());
				} else if (PointConstants.CATE_ID_AI_NB_MAXP_D.equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value12.getName(), v.getDv());
				} else if (PointConstants.CATE_ID_AM_NB_ENERGY_ALL.equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value5.getName(), v.getDv());
				} else if (PointConstants.CATE_ID_AM_NB_ENERGY_DAY.equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value6.getName(), v.getDv());
				} else if (PointConstants.CATE_ID_AI_NB_ZXYGDD_D.equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value8.getName(), v.getDv());
				} else if (PointConstants.CATE_ID_AI_NB_ZXYGDXS_D.equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value4.getName(), v.getDv());
				} else if ("AM-DNB-ZXYGZDN".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value1.getName(), v.getDv());
				} else if ("AM-DNB-ZXWGZDN".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value2.getName(), v.getDv());
				} else if ("AM-DNB-FXYGZDN".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value3.getName(), v.getDv());
				} else if ("AM-DNB-FXWGZDN".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value4.getName(), v.getDv());
				} else if ("AM-DNB-ZXYGDNJ".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value5.getName(), v.getDv());
				} else if ("AM-DNB-FXYGDNJ".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value6.getName(), v.getDv());
				} else if ("AM-DNB-ZXYGDNF".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value7.getName(), v.getDv());
				} else if ("AM-DNB-FXYGDNF".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value8.getName(), v.getDv());
				} else if ("AM-DNB-ZXYGDNP".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value9.getName(), v.getDv());
				} else if ("AM-DNB-FXYGDNP".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value10.getName(), v.getDv());
				} else if ("AM-DNB-ZXYGDNG".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value11.getName(), v.getDv());
				} else if ("AM-DNB-FXYGDNG".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value12.getName(), v.getDv());
				} else if ("AI-DNB-ZXYGZDN-H".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value13.getName(), v.getDv());
				} else if ("AI-DNB-ZXYGZDN-D".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value14.getName(), v.getDv());
				} else if ("AI-DNB-ZXYGZDN-M".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value15.getName(), v.getDv());
				} else if ("AI-DNB-ZXYGZDN-Y".equals(point.getCateId())) {
					data.put(DeviceMonitorColumn.value16.getName(), v.getDv());
				}
			}

			int sendCount = 0;
			for (Map<String, Object> data : datas.values()) {
				String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(e.getKey());
				try {
					kafkaService.sendMonitor(monitorCenterTopic, data, debug);
					++sendCount;
				} catch (Exception ex) {
					logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(data), ex);
				}
			}
			logger.info("send {} datas to monitorCenter", sendCount);
		}
	}

	private int getPointIds(List<Long> pids, String psrId, String cateId) {
		PointInfoDto point = pointCacheService.getPointByObjIdCateId(psrId, cateId);
		if (point != null) {
			pids.add(point.getPointId());
			return 1;
		} else {
			logger.error("point not exists, cateId: {}, psrId: {}", cateId, psrId);
			return 0;
		}
	}

	private void createStationMonitorRealData(Date date) throws Exception {
		Map<Integer, List<Long>> channelPids = new HashMap<Integer, List<Long>>();
		int pointCount = 0, stationCount = 0;
		
		for (Station s : stationCacheService.getStations().values()) {
			List<Long> pids = channelPids.get(s.getCalcChannel());
			if (pids == null) {
				pids = new ArrayList<Long>();
				channelPids.put(s.getCalcChannel(), pids);
			}
			
			pointCount += getPointIds(pids, s.getPsrId(), PointConstants.CATE_ID_STATION_POWER);	//功率
			pointCount += getPointIds(pids, s.getPsrId(), PointConstants.CATE_ID_STATION_MAXPOWER_D);	//日最大功率
			pointCount += getPointIds(pids, s.getPsrId(), PointConstants.CATE_ID_STATION_ENERGY_DAY);	//日发电量
			++stationCount;
		}
		logger.info("find {} points for {} stations", pointCount, stationCount);
		
		Date monitorDate = CommonService.trunc(date, TimeType.DAY);
		for (Entry<Integer, List<Long>> e : channelPids.entrySet()) {
			Map<String, Map<String, Object>> datas = new HashMap<String, Map<String, Object>>();
			
			Map<Long, PointValueDto> values = pointValueCacheService.getPointCurrValuesByPointIds(e.getKey(), e.getValue());
			logger.info("get {} point values for channel {}", values.size(), e.getKey());
			
			PointInfoDto point = null;
			for (PointValueDto v : values.values()) {
				if (v == null) continue;
				point = pointCacheService.getPointByPointId(v.getPid());
				if (point == null) continue;
				pointValueCacheService.setQByTime(v, point.getDataPeriod(), monitorDate);
				if (!pointValueCacheService.isValid(v)) continue;
				
				Map<String, Object> data = datas.get(point.getStationId());
				if (data == null) {
					Station s = stationCacheService.getStationByStationId(point.getStationId());
					if (s == null) continue;
					data = stationMonitorCacheService.createStationMonitorData(monitorDate.getTime(), s);
					datas.put(point.getStationId(), data);
				}
				
				if (PointConstants.CATE_ID_STATION_POWER.equals(point.getCateId())) {
					data.put(StationMonitorColumn.power.getName(), v.getDv());
					String runStatus = v.getDv() > 0.0 ?
							StationService.STATION_RUN_STATUS_RUN : StationService.STATION_RUN_STATUS_STOP;
					data.put(StationMonitorColumn.run_status.getName(), runStatus);

					if (!StationService.STATION_RUN_STATUS_RUN.equals(data.get(StationMonitorColumn.day_run_status.getName()))
					 && StationService.STATION_RUN_STATUS_RUN.equals(runStatus)) {
						data.put(StationMonitorColumn.day_run_status.getName(), StationService.STATION_RUN_STATUS_RUN);
					}
				} else if (PointConstants.CATE_ID_STATION_MAXPOWER_D.equals(point.getCateId())) {
					data.put(StationMonitorColumn.max_power.getName(), v.getDv());
					data.put(StationMonitorColumn.max_power_time.getName(), v.getDt());
				} else if (PointConstants.CATE_ID_STATION_ENERGY_DAY.equals(point.getCateId())) {
					data.put(StationMonitorColumn.energy_coll.getName(), v.getDv());
				}
			}
			
			int sendCount = 0;
			for (Map<String, Object> data : datas.values()) {
				String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(e.getKey());
				try {
					kafkaService.sendMonitor(monitorCenterTopic, data, debug);
					++sendCount;
				} catch (Exception ex) {
					logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(data), ex);
				}
			}
			logger.info("send {} datas to monitorCenter", sendCount);
		}
	}

}
