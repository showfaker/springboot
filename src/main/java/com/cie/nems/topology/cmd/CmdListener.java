package com.cie.nems.topology.cmd;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
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
import com.cie.nems.topology.CalcTopoService;

@Service
public class CmdListener {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.cmd:#{false}}")
	private boolean debug;

	@Autowired
	private KafkaConfig kafkaConfig;
	
	@Autowired
	private CalcTopoService calcTopoService;
	
	@Autowired
	private CmdService service;
	
	@Bean("cmdContainerFactory")
	public ConcurrentKafkaListenerContainerFactory<Integer, String> listenerContainer() throws NemsException {
		ConcurrentKafkaListenerContainerFactory<Integer, String> container = 
				new ConcurrentKafkaListenerContainerFactory<Integer, String>();
		container.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaConfig.consumerProps()));
		container.setAutoStartup(false);
		container.setConcurrency(calcTopoService.getConcurrency(CalcTopoService.CLIENT_ID_CMD));
		container.setBatchListener(false);
		
		return container;
	}

	@KafkaListener(id = CalcTopoService.CLIENT_ID_CMD+"-${cie.app.id}", 
			clientIdPrefix = CalcTopoService.CLIENT_ID_CMD+"-${cie.app.id}", 
			topics = {"${cmd-topic-name}"}, 
			//此处不同于其他Listener，每个拓扑实例的groupId必须不同，从而达到广播的效果
			groupId = CalcTopoService.CLIENT_ID_CMD+"-${cie.app.id}",
			containerFactory = "cmdContainerFactory")
	public void batchListener(ConsumerRecord<Integer, String> msg) {
		if (msg == null) return;
		
		//计数
		long id = getThreadId(Thread.currentThread().getId());
		
		Integer count = currCounts.get(id);
		if (count == null) {
			currCounts.put(id, 1);
		} else {
			currCounts.put(id, ++count);
		}
		
		//清除数据
		if (clean) {
			count = cleanCounts.get(id);
			if (count == null) count = 1;
			else count += 1;
			cleanCounts.put(id, count);
			if (count % 10000 == 0) {
				logger.info("clear {} msgs", count);
			}
			return;
		}
		
		if (debug) {
			logger.debug("receive - topic: {}, partition: {}, offset: {}, key: {}, value: {}", 
					msg.topic(), msg.partition(), msg.offset(), msg.key(), msg.value());
		}
		//正式消费
		try {
			service.execute(msg);
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
		if (clean) {
			for (Entry<Long, Integer> e : currCounts.entrySet()) {
				currCounts.put(e.getKey(), 0);
			}
		}
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
