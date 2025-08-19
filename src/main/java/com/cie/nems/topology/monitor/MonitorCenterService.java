package com.cie.nems.topology.monitor;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface MonitorCenterService {

	public void execute(List<ConsumerRecord<Integer, String>> msgs);

}
