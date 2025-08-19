package com.cie.nems.topology.calc.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.point.expression.TargetPointDto;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;

@Service
public class Expression01 implements ExpressionCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.expression-calc:#{false}}")
	private boolean debug;

	@Autowired
	private ExceptionService exceptionService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	@Lazy
	private ExpressionService expService;

	@Override
	public List<PointValueDto> calc(PointValueDto data, ExpressionDto exp, 
			Map<Integer, List<Map<String, Object>>> monitorDatasMap) {
		if (exp.getTargetPoints().get(0) == null) {
			logger.error("targetPoints[0] is null, ownPointId: {}", exp.getOwnPointId());
			return null;
		}
		if (CommonService.isEmpty(exp.getPoints1())) {
			logger.error("points1 is empty, ownPointId: {}", exp.getOwnPointId());
			return null;
		};
		
		List<PointValueDto> results = null;
		try {
			exp.setValues1(new ArrayList<Double>(CommonService.getListInitCapacity(exp.getPoints1().size())));
			
			for (Long pid : exp.getPoints1()) {
				if (pid == null) continue;

				// 测点可能不在同一个通道
//				PointInfoDto point = pointCacheService.getPointByPointId(pid);
//				if (point == null) {
//					logger.error("pid {} in expression(targetPointId: {}).point1 not exists", pid, exp.getOwnPointId());
//					exp.getValues1().add(null);
//					continue;
//				}
				// 目前redis不分区
//				PointValueDto value = pointValueCacheService.getPointCurrValueByPointId(point.getCalcChannel(), pid);
				PointValueDto value = pointValueCacheService.getPointCurrValueByPointId(0, pid);
				exp.getValues1().add(value == null ? null : value.getDv());
				if (debug) {
					logger.debug("set values1: {}", CommonService.toString(exp.getValues1()));
				}
			}
			
			Double value = null;
			for (Double v : exp.getValues1()) {
				if (v != null && !v.isNaN() && !v.isInfinite()) {
					if (value == null) value = v;
					else value += v;
				} else {
					if (ExpressionService.ILLAGEL_PARAM_STRATEGRY_RETURN.equals(exp.getIllegalParamStrategy())) {
						return null;
					}
				}
			}
			if (debug) {
				logger.debug("sum value: {}", value);
			}
			if (value != null && CommonService.isNotEmpty(exp.getStaticParamValues())) {
				Double ratio = exp.getStaticParamValues().get(0);
				if (ratio != null && ratio != 1.0) {
					if (debug) {
						logger.debug("ratio: {}", ratio);
					}
					value *= ratio;
					if (debug) {
						logger.debug("value: {}", value);
					}
				}
				Double offset = exp.getStaticParamValues().size() > 1 ? exp.getStaticParamValues().get(1) : null;
				if (offset != null && offset != 0.0) {
					if (debug) {
						logger.debug("offset: {}", offset);
					}
					value += offset;
					if (debug) {
						logger.debug("value: {}", value);
					}
				}
				
				TargetPointDto target = exp.getTargetPoints().get(0);
				if (target.getValidFloor() != null && value < target.getValidFloor()) {
					if (debug) {
						logger.debug("valid floor: {}", target.getValidFloor());
					}
					value = target.getValidFloor();
				}
				if (target.getValidCeil() != null && value > target.getValidCeil()) {
					value = target.getValidCeil();
					if (debug) {
						logger.debug("valid ceil: {}", target.getValidCeil());
					}
				}
				
				if (target.getPrecision() != null) {
					value = CommonService.round(value, target.getPrecision());
					if (debug) {
						logger.debug("round: {}, {}", target.getPrecision(), value);
					}
				}
				
				if (!value.isNaN() && !value.isInfinite()) {
					results = expService.updatePointValues(data, exp, target, value, monitorDatasMap);
				}
			}
		} catch(Exception ex) {
			logger.error("calc expression failed! {} - {}", exp.toString(), ex.getMessage());
			exceptionService.log(this.getClass().getName() + "-calc", "", ex);
		}
		return results;
	}

}
