package com.cie.nems.alarm.log;

import java.util.List;

public interface AlarmLogService {

	public List<AlarmLogs> getRealAlarms(List<Integer> channelIds);

	public int insertAlarmLogs(List<AlarmLogs> newAlarms);

	public int recoverAlarms(List<AlarmLogs> recoverAlarms);

}
