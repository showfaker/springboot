package com.cie.nems.topology.alarm.offline;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.alarm.rule.AlarmRule;
import com.cie.nems.common.Constants;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.parameter.AppParameter;
import com.cie.nems.common.parameter.ParamService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.device.DeviceService;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.pre.PointPreprocessRule;
import com.cie.nems.pre.PreProcessRuleService;
import com.cie.nems.station.Station;
import com.cie.nems.suntime.SunTime;
import com.cie.nems.topology.alarm.AlarmMsg;
import com.cie.nems.topology.alarm.AlarmService;
import com.cie.nems.topology.cache.alarm.rule.AlarmRuleCacheService;
import com.cie.nems.topology.cache.dataTime.DataTimeCacheService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.pre.PreRuleCacheService;
import com.cie.nems.topology.cache.station.StationCacheService;
import com.cie.nems.topology.cache.suntime.SunTimeCacheService;

@Service
public class OfflineAlarmServiceImpl implements OfflineAlarmService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private final Logger offlineLogger = LoggerFactory.getLogger("OfflineLog");
	
	@Value("${cie.app.debug.alarm:#{false}}")
	private boolean debug;

	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private ParamService paramService;

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private PreRuleCacheService preRuleCacheService;

	@Autowired
	private DataTimeCacheService dataTimeCacheService;

	@Autowired
	private SunTimeCacheService sunTimeCacheService;

	@Autowired
	private AlarmRuleCacheService alarmRuleCacheService;

	@Override
	public void execute(Date time) {
		OfflineAlarmCacheDto cache = initCache(time);
		
		//由于分发程序有参数可以配置在深夜不向后续计算拓扑分发数据，所以这时应该不做通讯状态告警
		Calendar c = Calendar.getInstance();
		c.setTime(time);
		int hhmm = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
		if (hhmm < cache.getStartHHmm() || hhmm > cache.getEndHHmm()) {
			logger.info("{} -> {} not in offline time {} - {}", CommonService.formatDate(Constants.dateFormatSecond, time), 
					hhmm, cache.getStartHHmm(), cache.getEndHHmm());
			return;
		}
		
		//更新每个设备的通讯状态
		setDeviceCommuStatus(cache);
		
		int MMdd = (c.get(Calendar.MONTH) + 1) * 100 + c.get(Calendar.DAY_OF_MONTH);
		//根据通讯状态发送通讯中断告警测点值
		sendOfflineAlarm(cache, hhmm, MMdd);
		
		//有些电站没有外线设备，需要通过判断逆变器在线数量来发送外线断电告警
		//这批电站都会单独配置alarm_source为02的告警规则
		sendStationOutlineAlarm(cache, hhmm, c);
	}

	private OfflineAlarmCacheDto initCache(Date time) {
		OfflineAlarmCacheDto cache = new OfflineAlarmCacheDto();
		cache.setStartRunTime(time);
		cache.setStartHHmm(400);
		cache.setEndHHmm(2200);
		cache.setStartAlarmHHmm(500);
		cache.setEndAlarmHHmm(1900);
		cache.setOfflineDuration(DEFAULT_OFFLINE_DURATION);
		cache.setOutlineDuration(DEFAULT_OUTLINE_DURATION);
		
		AppParameter param = paramService.getAppParameter(ParamService.PARAM_CODE_OFFLINE_ALARM);

		if (param != null) {
			if (!StringUtils.isBlank(param.getParam1())) {
				try {
					cache.setStartHHmm(Integer.parseInt(param.getParam1()));
				} catch (NumberFormatException e) {
					logger.error("read param start hhmm failed! {} is not a valied int, set to default value {}", 
							param.getParam1(), DEFAULT_START_HHMM);
					cache.setStartHHmm(DEFAULT_START_HHMM);
				}
			}
			if (!StringUtils.isBlank(param.getParam2())) {
				try {
					cache.setEndHHmm(Integer.parseInt(param.getParam2()));
				} catch (NumberFormatException e) {
					logger.error("read param end hhmm failed! {} is not a valied int, set to default value {}", 
							param.getParam2(), DEFAULT_END_HHMM);
					cache.setEndHHmm(DEFAULT_END_HHMM);
				}
			}
			//数据终端多长时间（ms）后判断为通讯中断
			if (!StringUtils.isBlank(param.getParam3())) {
				try {
					cache.setOfflineDuration(Long.parseLong(param.getParam3()));
				} catch (NumberFormatException e) {
					logger.error("read param offlineDuration failed! {} is not a valied int, set to default value {}", 
							param.getParam3(), DEFAULT_OFFLINE_DURATION);
					cache.setOfflineDuration(DEFAULT_OFFLINE_DURATION);
				}
			}
			//外线设备多长时间（ms）后判断为外线告警
			if (!StringUtils.isBlank(param.getParam4())) {
				try {
					cache.setOutlineDuration(Long.parseLong(param.getParam4()));
				} catch (NumberFormatException e) {
					logger.error("read param outlineDuration failed! {} is not a valied int, set to default value {}", 
							param.getParam4(), DEFAULT_OUTLINE_DURATION);
					cache.setOutlineDuration(DEFAULT_OUTLINE_DURATION);
				}
			}
			//外线设备或上级设备离线时，是否需要进行通讯中断告警？
			cache.setFilterByOutlineAndParent("1".equals(param.getParam5()));
		}
		
		logger.info("startRunTime: {}, startHHmm: {}, endHHmm: {}, offlineDuration: {}, outlineDuration: {}, filterByOutlineAndParent: {}", 
				CommonService.formatDate(Constants.dateFormatSecond, cache.getStartRunTime()), 
				cache.getStartHHmm(), cache.getEndHHmm(), cache.getOfflineDuration(), 
				cache.getOutlineDuration(), cache.getFilterByOutlineAndParent());

		return cache;
	}

	private void setDeviceCommuStatus(OfflineAlarmCacheDto cache) {
		Map<String, PointInfoDto> points = null;
		List<PointPreprocessRule> rules = null;
		List<Long> deadPointIds = new ArrayList<Long>(5);
		Map<Long, PointValueDto> values = null;
		Long duration = null;
		/* Map(channel, Values) */
		Map<Integer, List<PointValueDto>> channelCommuValues = new HashMap<Integer, List<PointValueDto>>();
		int deviceOthers = 0, deviceNoPoints = 0, deviceOnline = 0, deviceOffline = 0, deviceDead = 0;
		int newCommuValues = 0, noCommuPoint = 0;
		Date monitorDate = new Date(cache.getStartRunTime().getTime());
		monitorDate = CommonService.trunc(monitorDate, TimeType.DAY);
		
		for (Device d : deviceCacheService.getDevices().values()) {
			//过滤掉不处理的设备
			if (DeviceService.DEVICE_TYPE_ZC.equals(d.getDeviceType())
			 || DeviceService.DEVICE_TYPE_ZJ.equals(d.getDeviceType())
			 || DeviceService.DEVICE_TYPE_E2BOX.equals(d.getDeviceType())
			 || DeviceService.DEVICE_TYPE_BCJ.equals(d.getDeviceType())) {
				++deviceOthers;
				continue;
			}
			
			points = pointCacheService.getPointsByObjId(d.getPsrId());
			//无测点的设备不处理
			if (CommonService.isEmpty(points)) {
				++deviceNoPoints;
				offlineLogger.debug("calcTime: {}, deviceId: {} - has no points", 
						cache.getStartRunTime().getTime(), d.getDeviceId());
				continue;
			}
			
			//最近数据更新时间（注意，不是数据时间）
			Long lastUpdateTime = dataTimeCacheService.getDeviceUpdateTime(d.getDeviceId());
			offlineLogger.debug("calcTime: {}, deviceId: {} - lastUpdateTime: {}", 
					cache.getStartRunTime().getTime(), d.getDeviceId(), lastUpdateTime);
			
			//外线设备允许和普通设备使用不同的离线超时时长
			if (DeviceService.USE_FLAG_OUTLINE.equals(d.getUseFlag())) {
				duration = cache.getOutlineDuration();
				List<Device> list = cache.getOutlines().get(d.getStationId());
				if (list == null) {
					list = new ArrayList<Device>(3);
					cache.getOutlines().put(d.getStationId(), list);
				}
				list.add(d);
			} else {
				duration = cache.getOfflineDuration();
				List<Device> list = cache.getOtherDevices().get(d.getStationId());
				if (list == null) {
					list = new ArrayList<Device>(3);
					cache.getOtherDevices().put(d.getStationId(), list);
				}
				list.add(d);
			}
			
			d.setPreCommuStatus(d.getCommuStatus());
			offlineLogger.debug("calcTime: {}, deviceId: {} - previous commuStatus: {}", 
					cache.getStartRunTime().getTime(), d.getDeviceId(), d.getPreCommuStatus());
			
			if (lastUpdateTime == null || cache.getStartRunTime().getTime() - lastUpdateTime >= duration) {
				//如果无最近更新时间或已经长时间未更新，则先判断为离线
				d.setCommuStatus(COMMU_STATUS_OFFLINE_STR);
				offlineLogger.debug("calcTime: {}, deviceId: {} - new commuStatus: {} offline", 
						cache.getStartRunTime().getTime(), d.getDeviceId(), d.getCommuStatus());
				++deviceOffline;
			} else {
				d.setCommuStatus(COMMU_STATUS_ONLINE_STR);
				
				//对于在线的设备，还要看是否配置了死数规则的测点是否全部都是死数
				deadPointIds.clear();
				for (PointInfoDto p : points.values()) {
					//只看采集类测点
					if (p.getCateId().startsWith("A-") || p.getCateId().startsWith("AI-")) continue;
					
					rules = preRuleCacheService.getPreprocessRules(p.getPointId());
					if (CommonService.isEmpty(rules)) continue;
					
					for (PointPreprocessRule rule : rules) {
						if (PreProcessRuleService.RULE_TYPE_DEAD.equals(rule.getRuleType())) {
							deadPointIds.add(p.getPointId());
							break;
						}
					}
				}
				if (deadPointIds.size() > 0) {
					try {
						values = pointValueCacheService.getPointCurrValuesByPointIds(d.getCalcChannel(), deadPointIds);
					} catch (Exception e) {
						logger.error("get point curr value failed! {}", CommonService.toString(deadPointIds), e);
					}
					if (CommonService.isNotEmpty(values)) {
						int deadCount = 0;
						for (PointValueDto v : values.values()) {
							if (v == null) continue;
							if (PointConstants.POINT_VALUE_QUALITY_DEAD == v.getQ()) {
								++deadCount;
							}
						}
						if (deadCount == deadPointIds.size()) {
							//所有配置了死数规则的测点都处于死数状态，则认为通讯中断
							d.setCommuStatus(COMMU_STATUS_DEADVALUE_STR);
							offlineLogger.debug("calcTime: {}, deviceId: {} - has {} dead point and {} dead values, new commuStatus: {} dead value", 
									cache.getStartRunTime().getTime(), d.getDeviceId(), deadPointIds.size(), deadCount, d.getCommuStatus());
							++deviceDead;
						} else {
							//否则认为在线
							offlineLogger.debug("calcTime: {}, deviceId: {} - has {} dead point and {} dead values, new commuStatus: {} online", 
									cache.getStartRunTime().getTime(), d.getDeviceId(), deadPointIds.size(), deadCount, d.getCommuStatus());
							++deviceOnline;
						}
					} else {
						offlineLogger.debug("calcTime: {}, deviceId: {} - has {} dead point and 0 values, new commuStatus: {} online", 
								cache.getStartRunTime().getTime(), d.getDeviceId(), deadPointIds.size(), d.getCommuStatus());
						++deviceOnline;
					}
				} else {
					offlineLogger.debug("calcTime: {}, deviceId: {} - has no dead point, new commuStatus: {} online", 
							cache.getStartRunTime().getTime(), d.getDeviceId(), d.getCommuStatus());
					++deviceOnline;
				}
			}
			
			//生成通讯状态测点测点值
			PointInfoDto commuPoint = points.get(PointConstants.CATE_ID_COMM_STS);
			if (commuPoint != null) {
				d.setCommuPointId(commuPoint.getPointId());
				List<PointValueDto> commonValues = channelCommuValues.get(d.getCalcChannel());
				if (commonValues == null) {
					commonValues = new ArrayList<PointValueDto>(deviceCacheService.getDevices().size());
					channelCommuValues.put(d.getCalcChannel(), commonValues);
				}
				PointValueDto commuValue = new PointValueDto(null, commuPoint.getPointId(), 
						d.getCommuStatus(), cache.getStartRunTime().getTime(), null, 
						PointConstants.POINT_VALUE_QUALITY_VALID);
				commonValues.add(commuValue);
				cache.getDeviceCommuValues().put(d.getDeviceId(), commuValue);
				offlineLogger.debug("calcTime: {}, deviceId: {} - set commu pointId {} value to {}", 
						cache.getStartRunTime().getTime(), d.getDeviceId(), commuPoint.getPointId(), d.getCommuStatus());
				++newCommuValues;
			} else {
				offlineLogger.debug("calcTime: {}, deviceId: {} - has no commu point", 
						cache.getStartRunTime().getTime(), d.getDeviceId());
				++noCommuPoint;
			}
			
			//将通讯状态同步到监盘中心表
			sendToDeviceMonitor(monitorDate, cache.getStartRunTime(), d);
		}
		
		int pointValueUpdate = 0, recoverToAlarmTopic = 0;
		for (Entry<Integer, List<PointValueDto>> e : channelCommuValues.entrySet()) {
			String alarmTopic = kafkaService.getAlarmTopicName(e.getKey());
			if (e.getValue().size() > 0) {
				//批量更新通讯状态测点值
				try {
					pointValueCacheService.updatePointCurrValues(e.getKey(), e.getValue());
					pointValueUpdate += e.getValue().size();
				} catch (Exception ex) {
					logger.error("update commu point curr value failed! {}", e);
				}
				for (PointValueDto v : e.getValue()) {
					if (COMMU_STATUS_ONLINE_STR.equals(v.getV())) {
						//在线就持续往告警拓扑发数据，保证之前如果有告警的话可以复归
						try {
							kafkaService.sendPoint(alarmTopic, v, debug);
							++recoverToAlarmTopic;
						} catch (Exception ex) {
							logger.error("send to {} : {} failed!", alarmTopic, v, e);
						}
					}
				}
			}
		}
		logger.info("deviceTotal: {}, deviceOthers: {}, deviceNoPoints: {}, deviceOnline: {}, "
				+ "deviceOffline: {}, deviceDead: {}, noCommuPoint: {}, newCommuValues: {}, "
				+ "pointValueUpdate: {}, recoverToAlarmTopic: {}",
				deviceCacheService.getDevices().size(), deviceOthers, deviceNoPoints, deviceOnline, 
				deviceOffline, deviceDead, noCommuPoint, newCommuValues, pointValueUpdate, 
				recoverToAlarmTopic);
	}

	private void sendOfflineAlarm(OfflineAlarmCacheDto cache, int hhmm, int MMdd) {
		//对于离线设备，不能直接发送测点到告警拓扑取生成离线告警，而是要根据外线设备和上级设备的告警情况进行过滤，从而减少告警数量
		int total = 0, nullCommuStatus = 0, notInAlarmTime = 0, nullCommuValue = 0, outlineAlarmToAlarmTopic = 0;
		int outlineAlreadyAlarm = 0, parentAlreadyAlarm = 0, normalAlarmToAlarmTopic = 0;
		for (Device d : deviceCacheService.getDevices().values()) {
			++total;
			if (d.getCommuStatus() == null) {
				++nullCommuStatus;
				offlineLogger.debug("alarmTime: {}, deviceId: {} - commu status is null, will not send alarm data", 
						cache.getStartRunTime().getTime(), d.getDeviceId());
				continue;
			}
			
			if (!d.getCommuStatus().equals(d.getPreCommuStatus())
			 && !COMMU_STATUS_ONLINE_STR.equals(d.getCommuStatus())) {
				//设备通讯状态有变化，且是在线 -> 离线的
				offlineLogger.debug("alarmTime: {}, deviceId: {} - {} -> {}", 
						cache.getStartRunTime().getTime(), d.getDeviceId(), d.getPreCommuStatus(), d.getCommuStatus());
				
				//通讯中断告警只在指定时段进行，避免日出前和日落后设备正常停机造成的告警
				if (isNotInAlarmTime(d, cache, hhmm, MMdd)) {
					++notInAlarmTime;
					continue;
				}
				
				PointValueDto commuValue = cache.getDeviceCommuValues().get(d.getDeviceId());
				if (commuValue == null) {
					++nullCommuValue;
					offlineLogger.debug("alarmTime: {}, deviceId: {} - has no commu point value ", 
							cache.getStartRunTime().getTime(), d.getDeviceId());
					continue;
				}
				
				if (DeviceService.USE_FLAG_OUTLINE.equals(d.getUseFlag())) {
					//外线设备
					String alarmTopic = kafkaService.getAlarmTopicName(d.getCalcChannel());
					try {
						kafkaService.sendPoint(alarmTopic, commuValue, debug);
						offlineLogger.debug("alarmTime: {}, deviceId: {} - outline alarm", 
								cache.getStartRunTime().getTime(), d.getDeviceId());
						++outlineAlarmToAlarmTopic;
					} catch (Exception e) {
						logger.error("send to {} : {} failed!", alarmTopic, commuValue, e);
					}
				} else {
					boolean doAlarm = true;
					if (cache.getFilterByOutlineAndParent()) {
						//如果外线或上级设备已经告警，则不告警
						List<Device> outline = cache.getOutlines().get(d.getStationId());
						if (CommonService.isNotEmpty(outline)) {
							for (Device o : outline) {
								if (outline != null && !COMMU_STATUS_ONLINE_STR.equals(o.getCommuStatus())) {
									doAlarm = false;
									offlineLogger.debug("alarmTime: {}, deviceId: {} - outline {} already alarm", 
											cache.getStartRunTime().getTime(), d.getDeviceId(), o.getDeviceId());
									++outlineAlreadyAlarm;
									break;
								}
							}
						}
						if (doAlarm && StringUtils.isNotEmpty(d.getParentId())) {
							Device parent = deviceCacheService.getDeviceByDeviceId(d.getParentId());
							if (parent != null && !COMMU_STATUS_ONLINE_STR.equals(parent.getCommuStatus())) {
								++parentAlreadyAlarm;
								offlineLogger.debug("alarmTime: {}, deviceId: {} - parent {} already alarm", 
										cache.getStartRunTime().getTime(), d.getDeviceId(), parent.getDeviceId());
								doAlarm = false;
							}
						}
					}
					
					if (doAlarm) {
						String alarmTopic = kafkaService.getAlarmTopicName(d.getCalcChannel());
						try {
							kafkaService.sendPoint(alarmTopic, commuValue, debug);
							offlineLogger.debug("alarmTime: {}, deviceId: {} - normal alarm", 
									cache.getStartRunTime().getTime(), d.getDeviceId());
							++normalAlarmToAlarmTopic;
						} catch (Exception e) {
							logger.error("send to {} : {} failed!", alarmTopic, commuValue, e);
						}
					}
				}
			}
		}
		logger.info("total: {}, nullCommuStatus: {}, notInAlarmTime: {}, nullCommuValue: {}, outlineAlarmToAlarmTopic: {}, "
				+ "outlineAlreadyAlarm: {}, parentAlreadyAlarm: {}, normalAlarmToAlarmTopic: {}",
				total, nullCommuStatus, notInAlarmTime, nullCommuValue, outlineAlarmToAlarmTopic, 
				outlineAlreadyAlarm, parentAlreadyAlarm, normalAlarmToAlarmTopic);
	}

	private boolean isNotInAlarmTime(Device d, OfflineAlarmCacheDto cache, int hhmm, int MMdd) {
		Station s = stationCacheService.getStationByStationId(d.getStationId());
		List<SunTime> suntimes = null;
		if (s != null) {
			if (s.getCountyId() != null) {
				suntimes = sunTimeCacheService.getSunTimes(s.getCountyId());
			}
			if (CommonService.isEmpty(suntimes) && s.getCityId() != null) {
				suntimes = sunTimeCacheService.getSunTimes(s.getCityId());
			}
			if (CommonService.isEmpty(suntimes) && s.getProvinceId() != null) {
				suntimes = sunTimeCacheService.getSunTimes(s.getProvinceId());
			}
			if (CommonService.isEmpty(suntimes) && s.getCountryId() != null) {
				suntimes = sunTimeCacheService.getSunTimes(s.getProvinceId());
			}
		}
		if (CommonService.isNotEmpty(suntimes)) {
			for (SunTime t : suntimes) {
				if ((t.getStartDate() != null && MMdd >= t.getStartDate())
				 && (t.getEndDate() != null && MMdd <= t.getEndDate())) {
					//在有效期内
					if (hhmm <= t.getSunriseTime() || hhmm >= t.getSunsetTime()) {
						offlineLogger.debug("alarmTime: {}, deviceId: {} - not in alarm time(timeId: {}, {} - {}), will not send alarm data", 
								cache.getStartRunTime().getTime(), d.getDeviceId(), t.getSunTimeId(), t.getSunriseTime(), t.getSunsetTime());
						return true;
					} else {
						return false;
					}
				}
			}
		}

		//未指定地区的，按默认配置处理
		if (hhmm <= cache.getStartAlarmHHmm() || hhmm >= cache.getEndAlarmHHmm()) {
			offlineLogger.debug("alarmTime: {}, deviceId: {} - not in alarm time(defalt, {} - {}), will not send alarm data", 
					cache.getStartRunTime().getTime(), d.getDeviceId(), cache.getStartAlarmHHmm(), cache.getEndAlarmHHmm());
			return true;
		} else {
			return false;
		}
	}

	private void sendToDeviceMonitor(Date monitorDate, Date commuStatusTime, Device d) {
		Map<String, Object> data = deviceMonitorCacheService.createDeviceMonitorData(monitorDate.getTime(), d);
		data.put(DeviceMonitorColumn.commu_status.getName(), d.getCommuStatus());
		data.put(DeviceMonitorColumn.commu_status_time.getName(), commuStatusTime.getTime());
		Long lastDataTime = dataTimeCacheService.getDeviceDataTime(d.getDeviceId());
		if (lastDataTime != null) {
			data.put(DeviceMonitorColumn.last_data_time.getName(), lastDataTime);
		}
		String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(d.getCalcChannel());
		try {
			kafkaService.sendMonitor(monitorCenterTopic, data, debug);
		} catch (Exception e) {
			logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(data), e);
		}
	}

	private void sendStationOutlineAlarm(OfflineAlarmCacheDto cache, int hhmm, Calendar now) {
		int stationTotal = 0, ruleTotal = 0;
		Station s = null;
		for (List<AlarmRule> list : alarmRuleCacheService.getStationOutlineRules(null).values()) {
			if (CommonService.isEmpty(list)) continue; 
			++stationTotal;
			for (AlarmRule r : list) {
				if (r.getStationId() == null) continue;
				if (r.getBeginDate() != null && r.getBeginDate().after(cache.getStartRunTime())) continue;
				if (r.getEndDate() != null && r.getEndDate().before(cache.getStartRunTime())) continue;
				if (r.getBeginTime() != null && r.getBeginTime() > hhmm) continue;
				if (r.getEndTime() != null && r.getEndTime() < hhmm) continue;
				
				++ruleTotal;

				s = stationCacheService.getStationByStationId(r.getStationId());
				if (CommonService.isEmpty(s.getInverters())) {
					offlineLogger.debug("inverterOutline - stationId {} has no inverters", r.getStationId());
					continue;
				}
				
				int offline = 0, total = 0;
				for (Device d : s.getInverters()) {
					++total;
					if (!COMMU_STATUS_ONLINE_STR.equals(d.getCommuStatus())) {
						++offline;
					}
				}
				
				AlarmMsg msg = new AlarmMsg();
				msg.setAlarmMsgType(AlarmService.ALARM_MSG_TYPE_STATION_OUTLINE);
				msg.setRuleId(r.getRuleId());
				msg.setV(String.valueOf(CommonService.round(offline / total * 100.0, 2)));
				msg.setDt(cache.getStartRunTime().getTime());
				msg.setPsrId(s.getPsrId());
				msg.setStationId(r.getStationId());
				msg.setAlarmText("逆变器在线 " + (total - offline) + " 台，离线 " + offline + " 台");
				
				String alarmTopic = kafkaService.getAlarmTopicName(s.getCalcChannel());
				kafkaService.sendAlarmMsg(alarmTopic, msg, debug);
				offlineLogger.debug("inverterOutline - stationId {}, ruleId: {}, offline: {}, total: {}", 
						r.getStationId(), r.getRuleId(), offline, total);
			}
		}
		offlineLogger.debug("stationTotal: {}, ruleTotal: {}", stationTotal, ruleTotal);
	}

}
