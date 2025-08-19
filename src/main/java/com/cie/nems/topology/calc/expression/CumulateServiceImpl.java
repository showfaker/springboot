package com.cie.nems.topology.calc.expression;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.point.value.PointValueDto;

@Service
public class CumulateServiceImpl implements CumulateService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	/**
	 * 电量累计计算
	 * @param values
	 * @param capacity
	 * @return
	 */
	@Override
	public List<PointValueDto> cumulate(List<PointValueDto> values, Double capacity) {
		try {
			if (values == null || values.isEmpty()) return values;
			
			List<PointValueDto> newValues = new ArrayList<PointValueDto>(values.size());
			PointValueDto preValid = null;
			int validI = 0;
			double change = 0.0, cumuValue;
			long duration = 0L;
			
			for (int i=0; i<values.size(); ++i) {
				PointValueDto value = values.get(i);
				
				if (preValid == null) {
					newValues.add(value);
					if (/*valueService.isValid(value.getQ()) && */value.getDv() != null) {
						preValid = value;
						validI = i;
					}
				} else {
					change = 0.0;
					duration = 0L;
					if (value.getDv() != null && !value.getDv().isInfinite() && !value.getDv().isNaN()) {
						//先看与上一个值比较，变化率是否合法
						change = value.getDv() - values.get(i-1).getDv();
						duration = (value.getDt() - values.get(i-1).getDt()) / 1000L;
						if (change <= 0.0 || change / duration * 3600 > (capacity == null ? 5000.0 : capacity * 5.0)) {
							//不合法
							change = 0.0;
							//与上一个值对比不合法，就看与上一个合法值相比的情况
							change = value.getDv() - preValid.getDv();
							duration = (value.getDt() - preValid.getDt()) / 1000L;
							if (change <= 0.0 || change / duration * 3600 > (capacity == null ? 5000.0 : capacity * 5.0)) {
								change = 0.0;
							} else {
								preValid = value;
								validI = i;
							}
						} else {
							//合法
							if (validI == i-2
							 && values.get(i-1).getDv() - preValid.getDv() < 0.0
							 && value.getDv() - preValid.getDv() > 0) {
								//与上一个值比较合法也不能认为就可以直接使用
								//存在上上个值合法，上个值变小，然后当前值又变回比上上个值相等或大于一点的情况
								//这种时候认为上个值是偶尔的异常数据，要过滤掉，所以用当前值减上上个值
								change = value.getDv() - preValid.getDv();
							}
							preValid = value;
							validI = i;
						}
					}
					cumuValue = newValues.get(i-1).getDv() + change;
					//Long _id, Long pid, String v, Long dt, Long t, Integer q
					PointValueDto v = new PointValueDto(value.get_id(), value.getPid(), String.valueOf(cumuValue), 
							value.getDt(), value.getT(), value.getQ());
					v.setDv(cumuValue);
					newValues.add(v);
				}
			}
			return newValues;
		} catch(Exception e) {
			logger.error("cumulate failed!", e);
			return values;
		}
	}

	@Override
	public List<PointValueDto> calcHourValues(List<PointValueDto> values, PointInfoDto hourPoint, Date todayBegin, 
			List<PointValueDto> cacheValues, List<PointValueDto> dbValues) {
		List<PointValueDto> hValues = new ArrayList<PointValueDto>();
		PointValueDto last = null;
		Double min = null, max = null, dv = null;
		Long hourStart = CommonService.trunc(new Date(values.get(0).getDt()), TimeType.HOUR).getTime();
		Long hourEnd = CommonService.getEndTime(new Date(values.get(0).getDt()), TimeType.HOUR).getTime();
		int hcount = 0;
		for (PointValueDto value : values) {
			//if (!valueService.isValid(value.getQ())) continue;
			if (value.getDv() == null) continue;

			if (min == null || min > value.getDv()) {
				min = value.getDv();
			}
			if (max == null || max < value.getDv()) {
				max = value.getDv();
			}
			++hcount;
			if (value.getDt() > hourEnd) {
				if (min != null && max != null) {
					dv = max - min;
					PointValueDto newValue = new PointValueDto(null, hourPoint.getPointId(), String.valueOf(dv), hourStart, 
							null, PointConstants.POINT_VALUE_SOURCE_CALC + PointConstants.POINT_VALUE_QUALITY_VALID);
					newValue.setDv(dv);
					hValues.add(newValue);
					last = newValue;
				}
				hcount = 0;
				hourStart = CommonService.trunc(new Date(value.getDt()), TimeType.HOUR).getTime();
				hourEnd = CommonService.getEndTime(new Date(value.getDt()), TimeType.HOUR).getTime();
				min = max;
				max = null;
			}
		}
		if (hcount > 0) {
			if (min != null && max != null) {
				dv = max - min;
				PointValueDto newValue = new PointValueDto(null, hourPoint.getPointId(), String.valueOf(dv), hourStart, 
						null, PointConstants.POINT_VALUE_SOURCE_CALC + PointConstants.POINT_VALUE_QUALITY_VALID);
				newValue.setDv(dv);
				hValues.add(newValue);
				last = newValue;
			}
		}
		if (cacheValues != null && last != null && last.getDt() >= todayBegin.getTime()) {
			cacheValues.add(last);
		}
		if (dbValues != null) {
			dbValues.addAll(hValues);
		}
		return hValues;
	}

	@Override
	public PointValueDto calcDayValues(List<PointValueDto> values, PointInfoDto dayPoint, Calendar dateBegin, 
			Date todayBegin, List<PointValueDto> cacheValues, List<PointValueDto> dbValues) {
		Double min = null, max = null;
		for (PointValueDto value : values) {
			//if (!valueService.isValid(value.getQ())) continue;
			if (value.getDv() == null) continue;
			
			if (min == null || min > value.getDv()) {
				min = value.getDv();
			}
			if (max == null || max < value.getDv()) {
				max = value.getDv();
			}
		}
		if (max == null || min == null) return null;
		
		PointValueDto newValue = new PointValueDto();
		newValue.setPid(dayPoint.getPointId());
		newValue.setDv(max - min);
		newValue.setV(String.valueOf(max - min));
		newValue.setQ(PointConstants.POINT_VALUE_SOURCE_CALC + PointConstants.POINT_VALUE_QUALITY_VALID);
		newValue.setDt(dateBegin.getTimeInMillis());
		newValue.setT(null);

		if (cacheValues != null && newValue != null && newValue.getDt() >= todayBegin.getTime()) {
			cacheValues.add(newValue);
		}
		dbValues.add(newValue);
		return newValue;
	}

}
