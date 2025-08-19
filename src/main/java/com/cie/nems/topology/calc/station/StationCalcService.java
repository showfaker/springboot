package com.cie.nems.topology.calc.station;

import java.util.Date;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.cie.nems.point.PointInfoDto;

public interface StationCalcService {

	public void execute(List<ConsumerRecord<Integer, String>> msgs) throws Exception;

	public void calc(Date now);

	public boolean isStationCalcPoints(PointInfoDto point);

}
