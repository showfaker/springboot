package com.cie.nems.topology.calc.expression;

import java.util.List;
import java.util.Map;

import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface ExpressionCalcService {

	List<PointValueDto> calc(PointValueDto data, ExpressionDto exp, Map<Integer, List<Map<String, Object>>> monitorDatas);

}
