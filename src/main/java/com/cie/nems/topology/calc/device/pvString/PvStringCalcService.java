package com.cie.nems.topology.calc.device.pvString;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface PvStringCalcService {

	public void calcPvCurrent(PointValueDto data, Map<Integer, List<PointValueDto>> relaValues, 
			Calendar dateBegin);

}
