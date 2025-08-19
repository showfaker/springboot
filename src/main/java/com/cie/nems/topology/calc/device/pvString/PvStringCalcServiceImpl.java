package com.cie.nems.topology.calc.device.pvString;

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
import com.cie.nems.objRela.ObjRela;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;

@Service
public class PvStringCalcServiceImpl implements PvStringCalcService {
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
	public void calcPvCurrent(PointValueDto data, Map<Integer, List<PointValueDto>> relaValues, 
			Calendar dateBegin) {
		//只处理当日数据，历史数据在pre中入库即可
		if (data.getDt() < dateBegin.getTimeInMillis()) return;
		
		if (!pointValueCacheService.isValid(data)) return;
		
		ObjRela rela = deviceCacheService.getPointRelaDevice(data.getPid());
		if (rela == null || rela.getObjId2() == null) {
			logger.error("can't find pv string device id for pid {}", data.getPid());
			return;
		}
		
		Device pvStringDevice = deviceCacheService.getDeviceByDeviceId(rela.getObjId2());
		if (pvStringDevice == null) {
			logger.error("can't find pv string device for objId2 {} and pid {}", rela.getObjId2(), data.getPid());
			return;
		}
		
		//计算单位实时电流 AI-ZC-DWPVI
		calcDwpvi(pvStringDevice, data, relaValues);
		
		//更新电流曲线
		List<PointValueDto> values = pointValueCacheService.updatePointValueList(data, dateBegin);
		
		//计算小时平均电流
		//PointValueDto pviDayValue = null;
		try {
			/*pviDayValue = */calcPviHD(pvStringDevice, values, relaValues);
		} catch (Exception e) {
			logger.error("calcPviHD {} failed!", CommonService.toString(data), e);
		}
		
		//TODO 计算月平均电流
		//TODO 计算月平均电流
	}

	/**
	 * 计算单位电流（AI-ZC-DWPVI）：实时电流 / 组串装机容量（W）
	 * @param pvStringDevice
	 * @param data
	 * @param relaValues
	 */
	private void calcDwpvi(Device pvStringDevice, PointValueDto data, Map<Integer, List<PointValueDto>> relaValues) {
		PointInfoDto dwpviPoint = pointCacheService.getPointByObjIdCateId(pvStringDevice.getPsrId(), "AI-ZC-DWPVI");
		if (dwpviPoint == null) {
			logger.error("point not exist! psrId: {}, cateId: AI-ZC-DWPVI", pvStringDevice.getPsrId());
		} else {
			calcDwpvi(dwpviPoint, pvStringDevice, data, relaValues);
		}
	}
	
	private PointValueDto calcDwpvi(PointInfoDto dwpviPoint, Device pvStringDevice, PointValueDto data, Map<Integer, List<PointValueDto>> relaValues) {
		if (pvStringDevice.getCapacity() == null || pvStringDevice.getCapacity() == 0.0) {
			logger.error("pv string capacity is invalid, deviceId: {}", pvStringDevice.getDeviceId());
			return null;
		}
		
		return createPointValue(dwpviPoint, CommonService.round(data.getDv() / pvStringDevice.getCapacity(), 4), 
				data.getDt(), relaValues);
	}

	/**
	 * 用组串今日电流曲线计算小时平均电流（AI-ZC-PVI-H）和日平均电流（AI-ZC-PVI-D）
	 * @param pvStringDevice
	 * @param values
	 * @param relaValues
	 * @return 今日日均电流测点值对象
	 * @throws Exception
	 */
	private PointValueDto calcPviHD(Device pvStringDevice, List<PointValueDto> values,
			Map<Integer, List<PointValueDto>> relaValues) throws Exception {
		PointInfoDto pviHourPoint = pointCacheService.getPointByObjIdCateId(pvStringDevice.getPsrId(), "AI-ZC-PVI-H");
		if (pviHourPoint == null) {
			logger.error("point not exist! psrId: {}, cateId: AI-ZC-PVI-H", pvStringDevice.getPsrId());
			return null;
		}
		PointInfoDto pviDayPoint = pointCacheService.getPointByObjIdCateId(pvStringDevice.getPsrId(), "AI-ZC-PVI-D");
		if (pviDayPoint == null) {
			logger.error("point not exist! psrId: {}, cateId: AI-ZC-PVI-D", pvStringDevice.getPsrId());
			return null;
		}
		
		PointInfoDto dwpviHourPoint = pointCacheService.getPointByObjIdCateId(pvStringDevice.getPsrId(), "AI-ZC-DWPVI-H");
		if (dwpviHourPoint == null) {
			logger.error("point not exist! psrId: {}, cateId: AI-ZC-DWPVI-H", pvStringDevice.getPsrId());
		}

		int prePviHDataTime = getPrePviHDataTime(pviHourPoint);
		
		double hourSum = 0.0, daySum = 0.0;
		int hourCount = 0, dayCount = 0;
		Integer preHourTime = null, currHourTime = null;
		Long preDt = null;
		
		//StringBuffer log = new StringBuffer("\n");
		
		for (PointValueDto value : values) {
			if (!pointValueCacheService.isValid(value)) continue;
			
			//log.append(value.getDv()).append(" + ");
			
			currHourTime = getHourTime(value.getDt());
			
			if (preHourTime != null				//第一条记录不触发计算
					&& preHourTime < currHourTime) {	//时间夸小时了，触发小时数据计算
				if (currHourTime >= prePviHDataTime) {
					//因为电流曲线是一整天的数据，对于小于缓存中的小时时间的数据，之前已经计算过了，没必要重复计算
					PointValueDto v = createPointValue(pviHourPoint, CommonService.round(hourSum / hourCount, 4), 
							CommonService.trunc(new Date(preDt), TimeType.HOUR).getTime(), relaValues);
					
					//log.append(" -> ").append(hourSum).append("/").append(hourCount).append("=").append(v.getDv());
					
					if (dwpviHourPoint != null) {
						calcDwpvi(dwpviHourPoint, pvStringDevice, v, relaValues);
					}
				}
				hourSum = 0.0;
				hourCount = 0;
				
				//log.append("\npreHourTime: ").append(preHourTime).append(", currHourTime: ").append(currHourTime).append(", dt: ").append(value.getDt()).append("\n");
			}
			
			//累计数据
			hourSum += value.getDv();
			++hourCount;
			daySum += value.getDv();
			++dayCount;
			
			preHourTime = currHourTime;
			preDt = value.getDt().longValue();
		}
		
		if (hourCount > 0) {
			//计算最后一小时数据
			PointValueDto v = createPointValue(pviHourPoint, CommonService.round(hourSum / hourCount, 4), 
					CommonService.trunc(new Date(values.get(values.size() - 1).getDt()), TimeType.HOUR).getTime(), 
					relaValues);
			
			//log.append(" -> ").append(hourSum).append("/").append(hourCount).append("=").append(v.getDv());
			
			//log.append("\npreHourTime: ").append(preHourTime).append(", currHourTime: ").append(currHourTime).append(", dt: ").append(values.get(values.size() - 1).getDt()).append("\n");
			
			if (dwpviHourPoint != null) {
				calcDwpvi(dwpviHourPoint, pvStringDevice, v, relaValues);
			}
		}
		
		//logger.debug(log.toString());
		
		//计算日数据
		if (dayCount > 0) {
			PointValueDto v = createPointValue(pviDayPoint, CommonService.round(daySum / dayCount, 4), 
					CommonService.trunc(new Date(values.get(0).getDt()), TimeType.DAY).getTime(), relaValues);
			PointInfoDto dwpviDayPoint = pointCacheService.getPointByObjIdCateId(pvStringDevice.getPsrId(), "AI-ZC-DWPVI-D");
			if (dwpviDayPoint == null) {
				logger.error("point not exist! psrId: {}, cateId: AI-ZC-DWPVI-D", pvStringDevice.getPsrId());
			} else {
				calcDwpvi(dwpviDayPoint, pvStringDevice, v, relaValues);
			}
			return v;
		}
		
		return null;
	}

	private PointValueDto createPointValue(PointInfoDto pviHPoint, Double v, Long dt,
			Map<Integer, List<PointValueDto>> relaValues) {
		PointValueDto newValue = new PointValueDto();
		newValue.setPid(pviHPoint.getPointId());
		newValue.setV(String.valueOf(v));
		newValue.setDv(v);
		newValue.setDt(dt);
		List<PointValueDto> relaList = relaValues.get(pviHPoint.getCalcChannel());
		if (relaList == null) {
			relaList = new ArrayList<PointValueDto>();
			relaValues.put(pviHPoint.getCalcChannel(), relaList);
		}
		relaList.add(newValue);
		return newValue;
	}

	/**
	 * @param pviHPoint
	 * @return redis缓存中小时平均电流的数据时间，格式：yyyyMMddHH
	 * @throws Exception
	 */
	private int getPrePviHDataTime(PointInfoDto pviHPoint) throws Exception {
		PointValueDto prePviHValue = pointValueCacheService.getPointCurrValueByPointId(pviHPoint.getCalcChannel(), 
				pviHPoint.getPointId());
		if (prePviHValue != null && prePviHValue.getDt() != null) {
			return getHourTime(prePviHValue.getDt());
		}
		
		return 0;
	}

	private int getHourTime(Long dt) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(dt);
		
		return cal.get(Calendar.YEAR) * 1000000
				+ (cal.get(Calendar.MONTH) + 1) * 10000
				+ cal.get(Calendar.DAY_OF_MONTH) * 100
				+ cal.get(Calendar.HOUR_OF_DAY);
	}

}
