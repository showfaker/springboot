package com.cie.nems.topology.calc.device.inverter;

import java.util.Calendar;
import java.util.List;
import java.util.Map;

import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface InverterCalcService {

	public static final Double ZERO_POWER_VALUE = 0.1; 

	public void calcInverterPower(PointValueDto data, Map<Integer, List<PointValueDto>> relaValues, 
			Calendar dateBegin);

}
