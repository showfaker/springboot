package com.cie.nems.topology.calc.expression;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.point.expression.TargetPointDto;
import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface ExpressionService {

	/** 遇到非法参数（null, NaN, inifinity）忽略该参数，其他参数继续运算 */
	public static final String ILLAGEL_PARAM_STRATEGRY_IGNORE = "ignore";
	/** 遇到非法参数（null, NaN, inifinity）直接返回空 */
	public static final String ILLAGEL_PARAM_STRATEGRY_RETURN = "return";
	
	public static final String DYNAMIC_PARAM_OBJ_STATION_CAPACITY = "station.capacity";
	public static final String DYNAMIC_PARAM_OBJ_STATION_PARALLEL_CAPACITY = "station.parallelCapacity";

	public void execute(List<ConsumerRecord<Integer, String>> msgs);

	public List<PointValueDto> updatePointValues(PointValueDto data, ExpressionDto exp, 
			TargetPointDto target, Double value, Map<Integer, List<Map<String, Object>>> monitorDatasMap);

}
