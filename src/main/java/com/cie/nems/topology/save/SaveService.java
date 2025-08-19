package com.cie.nems.topology.save;

import java.util.Date;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface SaveService {

	public void execute(List<ConsumerRecord<Integer, String>> msgs);

	public void saveYxDayData(Date date);

}
