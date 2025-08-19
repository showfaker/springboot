package com.cie.nems.topology.distribute.ly;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.cie.nems.common.Constants;
import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.PointInfoService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Scope("prototype")
public class LyDistributeServiceImpl implements LyDistributeService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.distr:#{false}}")
	private boolean debug;

	@Value("${cie.distr.data-filter-interval-ms:#{180000}}")
	private long dataFilterInterval;
	
	@Value("${cie.distr.data-accept-begin:#{0}}")
	private int dataAcceptBegin;
	
	@Value("${cie.distr.data-accept-end:#{0}}")
	private int dataAcceptEnd;
	
	@Value("${cie.app.fowrard.enable:#{false}}")
	private boolean forwardEnable;
	
	@Value("${cie.app.fowrard.stationIds:#{null}}")
	private String forwardStationIds;
	
	@Value("${cie.app.fowrard.topics:#{'nems-forward'}}")
	private String forwardTopics;
	
	@Value("${cie.app.fowrard.debug:#{false}}")
	private boolean forwardDebug;

	@Autowired
	private ExceptionService exceptionService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private PointInfoService pointInfoService;
	
	@Autowired
	private KafkaService kafkaService;

	private ObjectMapper om = new ObjectMapper();

	@Override
	public void execute(ConsumerRecord<Integer, String> msg) {
		List<SensorData> datas = parseMessage(msg);
		
		if (CommonService.isEmpty(datas)) return;
		
		distribute(datas);
	}

	private List<SensorData> parseMessage(ConsumerRecord<Integer, String> msg) {
		List<SensorData> datas = null;
		try {
			datas = om.readValue(msg.value(), new TypeReference<List<SensorData>>() {});
		} catch (Exception e) {
			logger.error("parse msg failed! {} : {}", msg.value(), e.getMessage());
			exceptionService.log(this.getClass().getName() + "-parseMessage", "msg", e);
		}
		return datas;
	}

	private void distribute(List<SensorData> datas) {
		LastValueDto lastValue = null;
		String preTopic = null;
		//Calendar c = Calendar.getInstance();
		LocalTime time = null;
		int hhmm = 0;
		for (SensorData data : datas) {
			try {
				if (data.getPid() == null || StringUtils.isEmpty(data.getV()) || data.getDt() == null) continue;
				
				/* 为了节约服务器磁盘空间，设置接收数据的总开关时段 晚10点到凌晨3点的数据全部丢弃 */
				//c.setTimeInMillis(data.getDt());
				//hhmm = c.get(Calendar.HOUR_OF_DAY) * 100 + c.get(Calendar.MINUTE);
				time = Instant.ofEpochMilli(data.getDt())
						.atZone(ZoneOffset.ofHours(Constants.DEFAULT_ZONE_OFFSET_HOURS)).toLocalTime();
				hhmm = time.getHour() * 100 + time.getMinute();
				if (dataAcceptBegin >= 0 && hhmm < dataAcceptBegin) {
					continue;
				}
				if (dataAcceptEnd > 0 && hhmm >= dataAcceptEnd) {
					continue;
				}
				
				//查找测点资料
				PointInfoDto point = pointCacheService.getPointByPointId(data.getPid());
				//TODO 未配置资料的先放进来送到一个默认通道，让数据先入库，避免丢数据
				if (point == null) {
					logger.info("pid {} not exists", data.getPid());
					continue;
				}
				
				/* 为防止采集器故障或恶意数据，过滤高频数据，保障后续程序处理压力不会太大 */
				if (pointInfoService.isYxPoint(point)) {
					//遥信存在突发告警的情况，所以要结合时间和值是否变化一起判断
					lastValue = pointValueCacheService.getLastValueByPid(data.getPid());
					if (lastValue != null) {
						if (StringUtils.equals(data.getV(), lastValue.getV())) {
							if (lastValue.getDt() != null 
									&& Math.abs(data.getDt() - lastValue.getDt()) < dataFilterInterval) {
								continue;
							}
						}
					}
				} else {
					//遥测严格按时间过滤
					lastValue = pointValueCacheService.getLastValueByPid(data.getPid());
					if (lastValue != null && lastValue.getDt() != null 
							&& Math.abs(data.getDt() - lastValue.getDt()) < dataFilterInterval) {
						continue;
					}
				}
				pointValueCacheService.updateLastValue(data.getPid(), data.getDt(), data.getV());
				
				//有datas来自一个数据包，必定是一个采集器上传，所以必定是一个电站的测点
				if (point.getCalcChannel() == null) {
					logger.info("pid {} has no calcChannel", data.getPid());
					continue;
				}
				
				if (preTopic == null) {
					preTopic = kafkaService.getPreTopicName(point.getCalcChannel());
				}
				
				//TODO dt小于当前时间24小时的，判断为历史数据，单独走历史数据topic
				
				//根据channel分发
				kafkaService.sendPoint(preTopic, data, debug);
				
				forward(point, data);
			} catch(Exception e) {
				logger.error("distribute {} failed!", data.toString(), e);
				exceptionService.log(this.getClass().getName() + "-distribute", data, e);
			}
		}
	}

	/**
	 * Map(stationId, forwardTopic)
	 * @throws Exception 
	 */
	private void forward(PointInfoDto point, SensorData data) throws Exception {
		if (!forwardEnable) return;
		
		if (forwardStationMap == null) {
			initForward();
		}
		
		String topic = forwardStationMap.get(point.getStationId());
		if (topic != null) {
			kafkaService.sendPoint(topic, data, forwardDebug);
		}
	}

	private Map<String, String> forwardStationMap = null;
	private void initForward() {
		forwardStationMap = new HashMap<String, String>();
		
		String[] topics = StringUtils.split(forwardTopics, ';');
		if (topics == null || topics.length == 0) return;
		
		String[] idStrings = StringUtils.split(forwardStationIds, ';');
		if (idStrings == null || idStrings.length == 0) return;
		
		int minLength = topics.length > idStrings.length ? idStrings.length : topics.length;
		
		for (int i=0; i<minLength; ++i) {
			String[] ids = StringUtils.split(idStrings[i], ';');
			if (ids != null && ids.length > 0) {
				for (String id : ids) {
					if (StringUtils.isNotEmpty(id)) {
						forwardStationMap.put(id, topics[i]);
						logger.info("point of station {} will forward to {}", id, topics[i]);
					}
				}
			}
		}
	}

}
