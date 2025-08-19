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
public class Expression04 implements ExpressionCalcService {
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
		if (CommonService.isEmpty(exp.getPoints2())) {
			logger.error("points2 is empty, ownPointId: {}", exp.getOwnPointId());
			return null;
		};
		
		List<PointValueDto> results = null;
		try {
			exp.setValues1(new ArrayList<Double>(CommonService.getListInitCapacity(exp.getPoints1().size())));
			exp.setValues2(new ArrayList<Double>(CommonService.getListInitCapacity(exp.getPoints2().size())));
			
			setValues(exp.getPoints1(), exp.getValues1());
			setValues(exp.getPoints2(), exp.getValues2());
			
			Double sum1 = sum(exp.getValues1(), exp.getIllegalParamStrategy());
			if (sum1 == null)
				return null;
			
			Double ratio1 = exp.getStaticParamValues() == null || exp.getStaticParamValues().isEmpty() ? 
					null : exp.getStaticParamValues().get(0);
			if (sum1 != null && ratio1 != null && ratio1 != 1.0) {
				if (debug) {
					logger.debug("ratio1: {}", ratio1);
				}
				sum1 *= ratio1;
				if (debug) {
					logger.debug("sum1: {}", sum1);
				}
			}
			
			Double sum2 = sum(exp.getValues2(), exp.getIllegalParamStrategy());
			if (sum2 == null)
				return null;
			
			Double ratio2 = exp.getStaticParamValues() == null || exp.getStaticParamValues().size() <= 1 ? 
					null : exp.getStaticParamValues().get(1);
			if (sum2 != null && ratio2 != null && ratio2 != 1.0) {
				if (debug) {
					logger.debug("ratio2: {}", ratio2);
				}
				sum2 *= ratio2;
				if (debug) {
					logger.debug("sum2: {}", sum2);
				}
			}
			
			Double value = sum1 + sum2;
			

			Double offset = exp.getStaticParamValues() == null || exp.getStaticParamValues().size() <= 2 ? 
					null : exp.getStaticParamValues().get(2);
			if (offset != null && offset != 0.0) {
				if (debug) {
					logger.debug("offset: {}", offset);
				}
				value += offset;
				if (debug) {
					logger.debug("value: {}", value);
				}
			}
			
			if (value != null) {
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

	private Double sum(List<Double> values, String illegalParamStrategy) {
		Double value = null;
		for (Double v : values) {
			if (v != null && !v.isNaN() && !v.isInfinite()) {
				if (value == null) value = v;
				else value += v;
			} else {
				if (ExpressionService.ILLAGEL_PARAM_STRATEGRY_RETURN.equals(illegalParamStrategy)) {
					return null;
				}
			}
		}
		return value;
	}

	private void setValues(List<Long> points, List<Double> values) throws Exception {
		for (Long pid : points) {
			if (pid == null) continue;
			
			// 测点可能不在同一个通道
//			PointInfoDto point = pointCacheService.getPointByPointId(pid);
//			if (point == null) {
//				logger.error("pid {} in expression(targetPointId: {}).point1 not exists", pid, exp.getOwnPointId());
//				exp.getValues1().add(null);
//				continue;
//			}
			// 目前redis不分区
//			PointValueDto value = pointValueCacheService.getPointCurrValueByPointId(point.getCalcChannel(), pid);
			PointValueDto value = pointValueCacheService.getPointCurrValueByPointId(0, pid);
			
			values.add(value == null ? null : value.getDv());
			if (debug) {
				logger.debug("set values1: {}", CommonService.toString(values));
			}
		}
	}

}
