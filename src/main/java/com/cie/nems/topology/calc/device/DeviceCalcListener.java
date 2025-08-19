package com.cie.nems.topology.calc.device;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.NemsException;
import com.cie.nems.common.kafka.KafkaConfig;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.topology.CalcTopoService;

@Service
public class DeviceCalcListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.device-calc:#{false}}")
	private boolean debug;

	@Autowired
	private KafkaConfig kafkaConfig;
	
	@Autowired
	private CalcTopoService calcTopoService;
	
	@Autowired
	private DeviceCalcService service;
	
	@Bean("deviceCalcContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<Integer, String> listenerContainer() throws NemsException {
		ConcurrentKafkaListenerContainerFactory<Integer, String> container = 
				new ConcurrentKafkaListenerContainerFactory<Integer, String>();
		container.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaConfig.consumerProps()));
		container.setAutoStartup(false);
		container.setConcurrency(calcTopoService.getConcurrency(CalcTopoService.CLIENT_ID_DEVICE_CALC));
		container.setBatchListener(true);
		
		return container;
	}

	@KafkaListener(id = CalcTopoService.CLIENT_ID_DEVICE_CALC+"-${cie.app.id}", 
			clientIdPrefix = CalcTopoService.CLIENT_ID_DEVICE_CALC+"-${cie.app.id}", 
			topics = {"${device-calc-topic-name}" }, 
			groupId = CalcTopoService.CLIENT_ID_DEVICE_CALC, 
			containerFactory = "deviceCalcContainerFactory")
	public void batchListener(List<ConsumerRecord<Integer, String>> msgs) {
		if (CommonService.isEmpty(msgs)) return;
		
		//计数
		long id = getThreadId(Thread.currentThread().getId());
		
		Integer count = currCounts.get(id);
		if (count == null) {
			currCounts.put(id, msgs.size());
		} else {
			currCounts.put(id, count + msgs.size());
		}
		
		//清除数据
		if (clean) {
			count = cleanCounts.get(id);
			if (count == null) count = msgs.size();
			else count += msgs.size();
			cleanCounts.put(id, count);
			if (count % 10000 == 0) {
				logger.info("clear {} msgs", count);
			}
			return;
		}
		
		if (debug) {
			StringBuffer buf = new StringBuffer("receive");
			for (ConsumerRecord<Integer, String> msg : msgs) {
				buf.append("\ntopic: ").append(msg.topic())
				.append(", partition: ").append(msg.partition())
				.append(", offset: ").append(msg.offset())
				.append(", key: ").append(msg.key())
				.append(", value: ").append(msg.value());
			}
			logger.debug(buf.toString());
		}
		//正式消费
		try {
			service.execute(msgs);
		} catch(Exception e) {
			logger.error("execute failed!", e);
		}
	}


	private boolean clean;
	private Map<Long, Integer> preCounts = new HashMap<Long, Integer>();
	private Map<Long, Integer> currCounts = new HashMap<Long, Integer>();
	private Map<Long, Integer> cleanCounts = new HashMap<Long, Integer>();

	public boolean isClean() {
		return clean;
	}
	public void setClean(boolean clean) {
		this.clean = clean;
	}
	public Map<Long, Integer> getPreCounts() {
		return preCounts;
	}
	public void setPreCounts(Map<Long, Integer> preCounts) {
		this.preCounts = preCounts;
	}
	public Map<Long, Integer> getCurrCounts() {
		return currCounts;
	}
	public void setCurrCounts(Map<Long, Integer> currCounts) {
		this.currCounts = currCounts;
	}
	public Map<Long, Integer> getCleanCounts() {
		return cleanCounts;
	}
	public void setCleanCounts(Map<Long, Integer> cleanCounts) {
		this.cleanCounts = cleanCounts;
	}

	private static Long maxId = 0L;
	private static Map<Long, Long> clientIds = new ConcurrentHashMap<Long, Long>();
	private Long getThreadId(long threadId) {
		Long id = clientIds.get(threadId);
		if (id == null) {
			id = setThreadId(threadId);
		}
		return id;
	}
	private synchronized Long setThreadId(long threadId) {
		Long id = maxId++;
		clientIds.put(threadId, id);
		return id;
	}
}
