package com.cie.nems.topology.calc.device.battery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;

@Service
public class BatteryCalcServiceImpl implements BatteryCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.device-calc:#{false}}")
	private boolean debug;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Override
	public void calcBatteryCharge(PointValueDto data, Map<Integer, List<PointValueDto>> relaValues, 
			Calendar dateBegin) {
		//只处理当日数据，历史数据在pre中入库即可
		if (data.getDt() < dateBegin.getTimeInMillis()) return;
		
		Device d = deviceCacheService.getDeviceByDeviceId(data.getPoint().getDeviceId());
		if (d == null) {
			logger.error("device not exists {}", data.getPoint().getDeviceId());
			return;
		}
		
		//更新功率曲线
		List<PointValueDto> values = pointValueCacheService.updatePointValueList(data, dateBegin);
		
		calcDayCharge(data, dateBegin, values, relaValues);
		calcHourCharge(data, dateBegin, values, relaValues);
	}

	private void calcDayCharge(PointValueDto data, Calendar dateBegin, List<PointValueDto> values, 
			Map<Integer, List<PointValueDto>> relaValues) {
		String dayCateId = PointConstants.CATE_ID_AM_EBS_CHARGE_A.equals(data.getPoint().getCateId()) ?
							PointConstants.CATE_ID_AI_EBS_CHARGE_D : PointConstants.CATE_ID_AI_EBS_DISCHARGE_D;
		PointInfoDto dayPoint = pointCacheService.getPointByObjIdCateId(
							data.getPoint().getPsrId(), dayCateId);
		if (dayPoint == null) {
			logger.error("{} point not exists for psrId {}", dayCateId, data.getPoint().getPsrId());
		} else {
			double dayCharge = 0.0;
			if (CommonService.isNotEmpty(values)) {
				Double dayMin = values.get(0).getDv();
				Double dayMax = values.get(values.size() - 1).getDv();
				if (dayMin != null && dayMax != null) {
					dayCharge = dayMax - dayMin;
					logger.debug("psr: {}, cateId: {} -> dayMax: {} - dayMin: {} = dayChange: {}", 
							data.getPoint().getPsrId(), dayCateId, dayMax, dayMin, dayCharge);
				}
			}
			
			PointValueDto dayV = null;
			try {
				dayV = pointValueCacheService.getPointCurrValueByPointId(data.getPoint().getCalcChannel(), 
						dayPoint.getPointId());
			} catch (Exception e) {
				logger.error("get curr value for {} failed!", dayPoint.getPointId());
			}
			
			if (dayV == null || (dayV.getDv() - dayCharge != 0.0)) {
				dayV = new PointValueDto();
				dayV.setPid(dayPoint.getPointId());
				dayV.setV(String.valueOf(dayCharge));
				dayV.setDt(data.getDt());
				List<PointValueDto> relaList = relaValues.get(dayPoint.getCalcChannel());
				if (relaList == null) {
					relaList = new ArrayList<PointValueDto>();
					relaValues.put(dayPoint.getCalcChannel(), relaList);
				}
				relaList.add(dayV);
			}
		}
	}

	private void calcHourCharge(PointValueDto data, Calendar dateBegin, List<PointValueDto> values, 
			Map<Integer, List<PointValueDto>> relaValues) {
		String hourCateId = PointConstants.CATE_ID_AM_EBS_CHARGE_A.equals(data.getPoint().getCateId()) ?
							PointConstants.CATE_ID_AI_EBS_CHARGE_H : PointConstants.CATE_ID_AI_EBS_DISCHARGE_H;
		PointInfoDto hourPoint = pointCacheService.getPointByObjIdCateId(
							data.getPoint().getPsrId(), hourCateId);
		if (hourPoint == null) {
			logger.error("{} point not exists for psrId {}", hourCateId, data.getPoint().getPsrId());
		} else {
			double hourCharge = 0.0;
			if (CommonService.isNotEmpty(values)) {
				Date hourBegin = CommonService.trunc(new Date(data.getDt()), TimeType.HOUR);
				
				Double preHourMax = null;
				Double hourMin = null;
				Double hourMax = null;
				for (PointValueDto v : values) {
					if (v.getDt() < hourBegin.getTime()) {
						preHourMax = v.getDv();
					} else {
						if (hourMin == null)
							hourMin = v.getDv();
						if (hourMax == null || hourMax < v.getDv())
							hourMax = v.getDv();
					}
				}
				
				
				if (hourMax != null) {
					if (preHourMax != null) {
						hourCharge = hourMax - preHourMax;
						logger.debug("psr: {}, cateId: {} -> hourMax: {} - preHourMax: {} = hourCharge: {}", 
								data.getPoint().getPsrId(), hourCateId, hourMax, preHourMax, hourCharge);
					} else if (hourMin != null) {
						hourCharge = hourMax - hourMin;
						logger.debug("psr: {}, cateId: {} -> hourMax: {} - hourMin: {} = hourCharge: {}", 
								data.getPoint().getPsrId(), hourCateId, hourMax, hourMin, hourCharge);
					}
				}
			}
			
			PointValueDto hourV = null;
			try {
				hourV = pointValueCacheService.getPointCurrValueByPointId(data.getPoint().getCalcChannel(), 
						hourPoint.getPointId());
			} catch (Exception e) {
				logger.error("get curr value for {} failed!", hourPoint.getPointId());
			}
			
			if (hourV == null || (hourV.getDv() - hourCharge != 0.0)) {
				hourV = new PointValueDto();
				hourV.setPid(hourPoint.getPointId());
				hourV.setV(String.valueOf(hourCharge));
				hourV.setDt(data.getDt());
				List<PointValueDto> relaList = relaValues.get(hourPoint.getCalcChannel());
				if (relaList == null) {
					relaList = new ArrayList<PointValueDto>();
					relaValues.put(hourPoint.getCalcChannel(), relaList);
				}
				relaList.add(hourV);
			}
		}
	}

}
