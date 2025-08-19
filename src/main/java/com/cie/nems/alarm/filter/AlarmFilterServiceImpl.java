package com.cie.nems.alarm.filter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlarmFilterServiceImpl implements AlarmFilterService {

	@Autowired
	private AlarmFilterDao alarmFilterDao;
	
	@Override
	public List<AlarmFilter> getAlarmFilters(List<Integer> channelIds) {
		return alarmFilterDao.getAlarmFilters(channelIds);
	}

}
