package com.cie.nems.topology.cmd;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface CmdService {

	public void execute(ConsumerRecord<Integer, String> msg);

}
