package com.cie.nems.common.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

@Component
public class KafkaSendResultHandler implements ProducerListener<Integer, String> {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void onError(ProducerRecord<Integer, String> producerRecord, Exception exception) {
		logger.info("kafka msg send failed : " + producerRecord.toString());
	}
}