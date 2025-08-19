package com.cie.nems.topology.calc.station.algorithm;

import java.util.List;

import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface StationPowerCalcService {

	public void calc(PointValueDto deviceData, List<PointValueDto> cacheValues, List<PointValueDto> dbValues) throws Exception;

}
