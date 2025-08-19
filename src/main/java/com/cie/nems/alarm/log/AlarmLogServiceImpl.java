package com.cie.nems.alarm.log;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlarmLogServiceImpl implements AlarmLogService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AlarmLogDao alarmLogDao;
	
	@Override
	public List<AlarmLogs> getRealAlarms(List<Integer> channelIds) {
		return alarmLogDao.getRealAlarms(channelIds);
	}

	@Transactional
	@Override
	public int insertAlarmLogs(List<AlarmLogs> alarms) {
		int rows = alarmLogDao.insert(alarms);
		logger.debug("insert {}/{} new alarms into alarm_logs", rows, alarms.size());
		return rows;
	}

	@Transactional
	@Override
	public int recoverAlarms(List<AlarmLogs> recoverAlarms) {
		int rows = alarmLogDao.updateAlarmLogs(recoverAlarms);
		logger.debug("update {} alarm_logs", rows);
		return rows;
	}

}
