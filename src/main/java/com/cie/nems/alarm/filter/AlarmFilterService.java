package com.cie.nems.alarm.filter;

import java.util.List;

public interface AlarmFilterService {

	public List<AlarmFilter> getAlarmFilters(List<Integer> channelIds);

}
