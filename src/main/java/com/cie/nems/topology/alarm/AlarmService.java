package com.cie.nems.topology.alarm;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface AlarmService {

	public static final String ALARM_MSG_TYPE_COLUMN_NAME = "alarmMsgType";
	public static final String ALARM_MSG_TYPE_STATION_OUTLINE = "1";

	public static final String ALARM_SOURCE_REAL = "01";	//实时告警程序
	public static final String ALARM_SOURCE_OFFLINE = "02";	//离线告警程序
	public static final String ALARM_SOURCE_GROUP = "03";	//分组分析告警程序

	public void execute(List<ConsumerRecord<Integer, String>> msgs);

}
