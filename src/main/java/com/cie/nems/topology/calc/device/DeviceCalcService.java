package com.cie.nems.topology.calc.device;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.cie.nems.point.PointInfoDto;

public interface DeviceCalcService {

	public void execute(List<ConsumerRecord<Integer, String>> msgs) throws Exception;

	public boolean isDeviceCalcPoints(PointInfoDto point);

}
