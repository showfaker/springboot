package com.cie.nems.topology.calc.expression;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface CumulateService {

	public List<PointValueDto> cumulate(List<PointValueDto> values, Double capacity);

	public List<PointValueDto> calcHourValues(List<PointValueDto> values, PointInfoDto hourPoint, Date todayBegin, 
			List<PointValueDto> cacheValues, List<PointValueDto> dbValues);

	public PointValueDto calcDayValues(List<PointValueDto> values, PointInfoDto dayPoint, Calendar dateBegin, 
			Date todayBegin, List<PointValueDto> cacheValues, List<PointValueDto> dbValues);

}
