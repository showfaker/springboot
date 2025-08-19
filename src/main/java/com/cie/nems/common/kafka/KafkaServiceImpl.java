package com.cie.nems.common.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.NemsException;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.topology.CalcTopoCfg;
import com.cie.nems.topology.CalcTopoService;
import com.cie.nems.topology.alarm.AlarmMsg;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;

@Service
public class KafkaServiceImpl implements KafkaService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.id}")
	private String appId;
	
	@Autowired
	private CalcTopoService calcTopoService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private KafkaListenerEndpointRegistry registry;

	@Autowired
	private KafkaTemplate<String, String> kafkaTemp;

	@Override
	public void startKafkaListeners() throws NemsException {
		StringBuffer info = new StringBuffer();
		info.append("\n****************************************************************");
		
		Map<String, CalcTopoCfg> cfgs = calcTopoService.getCalcTopoCfgs();
		
		int count = 0;
		
		for (Entry<String, CalcTopoCfg> e : cfgs.entrySet()) {
			if (CalcTopoService.CALC_TOPO_STATUS_ACTIVE.equals(e.getValue().getTopoStatus())) {
				info.append(startKafkaListener(e.getValue().getClientPrefix() + "-" + appId));
				++count;
			}
		}

		if (count == 0) {
			info.append("\n* find 0 listeners to start");
		}
		info.append("\n****************************************************************");
		logger.info(info.toString());
	}

	private String startKafkaListener(String id) {
		try {
			if (!registry.getListenerContainer(id).isRunning()) {
				registry.getListenerContainer(id).start();
			}
			registry.getListenerContainer(id).resume();
		} catch(Exception e) {
			logger.error("start listener {} failed!", id);
			throw e;
		}
		return "\nstarting listener " + id + " ...";
	}
	
	@Override
	public void sendPoint(String topic, SensorData data, boolean debug) throws Exception {
		//用pid最后一位做为key，保证相同测点落入同一个partition，从而保证告警和入库 的顺序
		String key = String.valueOf(data.getPid() % 32);
		String msg = pointValueCacheService.serializer(data);
		kafkaTemp.send(topic, key, msg);
		if (debug) {
			logger.debug("send to {} : {} : {}", topic, key, msg);
		}
	}
	
	@Override
	public void sendPoint(String topic, PointValueDto data, boolean debug) throws Exception {
		//用pid最后一位做为key，保证相同测点落入同一个partition，从而保证告警和入库 的顺序
		String key = String.valueOf(data.getPid() % 32);
		String msg = pointValueCacheService.serializer(data);
		kafkaTemp.send(topic, key, msg);
		if (debug) {
			logger.debug("send to {} : {} : {}", topic, key, msg);
		}
	}
	
	@Override
	public void sendAlarmMsg(String topic, AlarmMsg msg, boolean debug) {
		kafkaTemp.send(topic, msg.toString());
		if (debug) {
			logger.debug("send to {} : {}", topic, msg);
		}
	}

	@Override
	public void sendMonitor(String topic, Map<String, Object> data, boolean debug) {
		String msg = CommonService.toString(data);
		kafkaTemp.send(topic, msg);
		if (debug) {
			logger.debug("send to {} : {}", topic, msg);
		}
	}
//
//	@Deprecated
//	@Override
//	public void sendToMongo(PointValueDto data, boolean debug) {
//		String key = String.valueOf(data.getPid() % 32);
//		//String msg = pointCacheService.serializer(data);
//		String msg = data.getPid()+","+data.getV()+","+data.getDt()+","+data.getQ();
//		kafkaTemp.send("my-topic-way-out-mongo", key, msg);
//		if (debug) {
//			logger.debug("send to {} : {} : {}", "my-topic-way-out-mongo", key, msg);
//		}
//	}
//	
	@Value("${cie.pre.topic-prefix}")
	private String preTopicPrefix;
	private Map<Integer, String> preTopicNames = new HashMap<Integer, String>();
	@Override
	public String getPreTopicName(Integer channel) {
		String topic = preTopicNames.get(channel);
		if (topic == null) {
			topic = preTopicPrefix + "-" + channel;
			preTopicNames.put(channel, topic);
		}
		return topic;
	}

	@Value("${cie.alarm.topic-prefix}")
	private String alarmTopicPrefix;
	private Map<Integer, String> alarmTopicNames = new HashMap<Integer, String>();
	@Override
	public String getAlarmTopicName(Integer channel) {
		String topic = alarmTopicNames.get(channel);
		if (topic == null) {
			topic = alarmTopicPrefix + "-" + channel;
			alarmTopicNames.put(channel, topic);
		}
		return topic;
	}

	@Value("${cie.save.topic-prefix}")
	private String saveTopicPrefix;
	private Map<Integer, String> saveTopicNames = new HashMap<Integer, String>();
	@Override
	public String getSaveTopicName(Integer channel) {
		String topic = saveTopicNames.get(channel);
		if (topic == null) {
			topic = saveTopicPrefix + "-" + channel;
			saveTopicNames.put(channel, topic);
		}
		return topic;
	}

	@Value("${cie.expression-calc.topic-prefix}")
	private String expressionCalcTopicPrefix;
	private Map<Integer, String> expressionCalcTopicNames = new HashMap<Integer, String>();
	@Override
	public String getExpressionCalcTopicName(Integer channel) {
		String topic = expressionCalcTopicNames.get(channel);
		if (topic == null) {
			topic = expressionCalcTopicPrefix + "-" + channel;
			expressionCalcTopicNames.put(channel, topic);
		}
		return topic;
	}

	@Value("${cie.device-calc.topic-prefix}")
	private String deviceCalcTopicPrefix;
	private Map<Integer, String> deviceCalcTopicNames = new HashMap<Integer, String>();
	@Override
	public String getDeviceCalcTopicName(Integer channel) {
		String topic = deviceCalcTopicNames.get(channel);
		if (topic == null) {
			topic = deviceCalcTopicPrefix + "-" + channel;
			deviceCalcTopicNames.put(channel, topic);
		}
		return topic;
	}

	@Value("${cie.station-calc.topic-prefix}")
	private String stationCalcTopicPrefix;
	private Map<Integer, String> stationCalcTopicNames = new HashMap<Integer, String>();
	@Override
	public String getStationCalcTopicName(Integer channel) {
		String topic = stationCalcTopicNames.get(channel);
		if (topic == null) {
			topic = stationCalcTopicPrefix + "-" + channel;
			stationCalcTopicNames.put(channel, topic);
		}
		return topic;
	}

	@Value("${cie.monitor-center.topic-prefix}")
	private String monitorCenterTopic;
	private Map<Integer, String> monitorCenterTopicNames = new HashMap<Integer, String>();
	@Override
	public String getMonitorCenterTopicName(Integer channel) {
		String topic = monitorCenterTopicNames.get(channel);
		if (topic == null) {
			topic = monitorCenterTopic + "-" + channel;
			monitorCenterTopicNames.put(channel, topic);
		}
		return topic;
	}

}
