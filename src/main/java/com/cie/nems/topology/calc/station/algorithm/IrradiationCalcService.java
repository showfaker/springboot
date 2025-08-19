package com.cie.nems.topology.calc.station.algorithm;

import java.util.List;

import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface IrradiationCalcService {

	public void calc(PointValueDto data, List<PointValueDto> cacheValues, List<PointValueDto> dbValues);

}
