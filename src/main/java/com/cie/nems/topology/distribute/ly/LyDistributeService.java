package com.cie.nems.topology.distribute.ly;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface LyDistributeService {

	public void execute(ConsumerRecord<Integer, String> msg);

}
