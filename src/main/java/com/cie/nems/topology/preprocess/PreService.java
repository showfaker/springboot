package com.cie.nems.topology.preprocess;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface PreService {

	public void execute(List<ConsumerRecord<Integer, String>> msgs);

}
