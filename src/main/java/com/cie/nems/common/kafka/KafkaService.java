package com.cie.nems.common.kafka;

import java.util.Map;

import com.cie.nems.common.exception.NemsException;
import com.cie.nems.topology.alarm.AlarmMsg;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;

public interface KafkaService {

	public void startKafkaListeners() throws NemsException;

	public void sendPoint(String topic, SensorData data, boolean debug) throws Exception;

	public void sendPoint(String topic, PointValueDto data, boolean debug) throws Exception;
	
	public void sendAlarmMsg(String topic, AlarmMsg msg, boolean debug);

	public void sendMonitor(String topic, Map<String, Object> data, boolean debug);

	public String getPreTopicName(Integer channel);
	public String getAlarmTopicName(Integer channel);
	public String getSaveTopicName(Integer channel);
	public String getExpressionCalcTopicName(Integer channel);
	public String getDeviceCalcTopicName(Integer channel);
	public String getStationCalcTopicName(Integer channel);
	public String getMonitorCenterTopicName(Integer channel);

}
