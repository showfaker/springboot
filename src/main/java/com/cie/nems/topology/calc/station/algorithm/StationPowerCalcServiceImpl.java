package com.cie.nems.topology.calc.station.algorithm;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.device.Device;
import com.cie.nems.monitor.station.StationMonitorColumn;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.station.Station;
import com.cie.nems.station.StationService;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.station.StationCacheService;

@Service
public class StationPowerCalcServiceImpl implements StationPowerCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.station-calc:#{false}}")
	private boolean debug;

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private KafkaService kafkaService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@Override
	public void calc(PointValueDto deviceData, List<PointValueDto> cacheValues, List<PointValueDto> dbValues) throws Exception {
		Station station = stationCacheService.getStationByStationId(deviceData.getPoint().getStationId());
		if (station == null) {
			logger.error("stationId {} not exists", deviceData.getPoint().getStationId());
			return;
		}
		
		String cateId = PointConstants.CATE_ID_AM_NB_POWER.equals(deviceData.getPoint().getCateId()) ?
				PointConstants.CATE_ID_AI_DZ_GLNB : PointConstants.CATE_ID_AI_DZ_GLDNB;
		PointInfoDto point = pointCacheService.getPointByObjIdCateId(station.getPsrId(), cateId);
		if (point == null) {
			logger.error("can't find {} point for station(psrId: {}, stationId: {})", 
					cateId, station.getPsrId(), station.getStationId());
			return;
		}
		
		PointValueDto value = pointValueCacheService.getPointCurrValueByPointId(station.getCalcChannel(), 
				point.getPointId());
		if (value == null || deviceData.getDt() - value.getDt() > 60000L) {
			if (value == null) {
				value = new PointValueDto();
				value.setPid(point.getPointId());
			}
			//由于站内有很多逆变器，所以不必每来一个逆变器的功率就马上更新电站对应功率
			value.setPoint(point);
			
			if (PointConstants.CATE_ID_AM_NB_POWER.equals(deviceData.getPoint().getCateId())) {
				value = calcStationDevicePower(station, value, station.getInverters(), 
						deviceData.getPoint().getCateId(), deviceData.getDt(), cacheValues, dbValues);
				
				if (value != null && StationService.POWER_SOURCE_INVERTER.equals(station.getPowerSource())) {
					calcStationPower(station, value, cacheValues, dbValues);
				}
			} else if (PointConstants.CATE_ID_AM_DNB_ZXYGZGL.equals(deviceData.getPoint().getCateId())
					&& StationService.POWER_SOURCE_METER_ACTIVE.equals(station.getPowerSource())) {
				value = calcStationDevicePower(station, value, station.getMeters(), 
						deviceData.getPoint().getCateId(), deviceData.getDt(), cacheValues, dbValues);
				
				if (value != null) calcStationPower(station, value, cacheValues, dbValues);
			} else if (PointConstants.CATE_ID_AM_DNB_FXYGZGL.equals(deviceData.getPoint().getCateId())
					&& StationService.POWER_SOURCE_METER_REACTIVE.equals(station.getPowerSource())) {
				value = calcStationDevicePower(station, value, station.getMeters(), 
						deviceData.getPoint().getCateId(), deviceData.getDt(), cacheValues, dbValues);
				
				if (value != null) calcStationPower(station, value, cacheValues, dbValues);
			}
		}
	}

	private PointValueDto calcStationDevicePower(Station station, PointValueDto value, 
			List<Device> devices, String deviceCateId, Long currDataDt, List<PointValueDto> cacheValues, 
			List<PointValueDto> dbValues) throws Exception {
		List<PointInfoDto> devicePoints = new LinkedList<PointInfoDto>();
		Map<Long, Double> pointCapacity = new HashMap<Long, Double>();
		for (Device d : devices) {
			PointInfoDto devicePoint = pointCacheService.getPointByObjIdCateId(d.getPsrId(), deviceCateId);
			if (devicePoint != null) {
				devicePoints.add(devicePoint);
				pointCapacity.put(devicePoint.getPointId(), d.getCapacity());
			}
		}
		Map<Long, PointValueDto> deviceValues = pointValueCacheService.getPointCurrValuesByPoints(
				station.getCalcChannel(), devicePoints);
		if (CommonService.isEmpty(deviceValues)) return null;
		
		//电站数据时间都按5分钟进行规整
		currDataDt -= currDataDt % 300000L;
		
		Double capacity = null;
		Double stationPower = 0.0;
		for (PointValueDto dv : deviceValues.values()) {
			if (dv == null) continue;
			//判断设备功率是否超过装机容量太多
			capacity = pointCapacity.get(dv.getPid());
			if (capacity != null && dv.getDv() > capacity * 3) continue;
			
			//超过1小时未更新的数据不参与计算
			if (currDataDt - dv.getDt() > 3600000L) continue;
			
			stationPower += dv.getDv();
		}
		
		value.setDv(stationPower);
		value.setV(stationPower.toString());
		value.setDt(currDataDt);
		
		cacheValues.add(value);
		dbValues.add(value);
		
		return value;
	}

	private void calcStationPower(Station station, PointValueDto value, List<PointValueDto> cacheValues, List<PointValueDto> dbValues) {
		PointInfoDto point = pointCacheService.getPointByObjIdCateId(station.getPsrId(), 
				PointConstants.CATE_ID_STATION_POWER);
		if (point == null) {
			logger.error("can't find {} point for station(psrId: {}, stationId: {})", 
					PointConstants.CATE_ID_STATION_POWER, station.getPsrId(), station.getStationId());
			return;
		}
		
		//将逆变器功率/电表功率覆盖给电站功率
		PointValueDto v = new PointValueDto();
		v.setPid(point.getPointId());
		v.setV(value.getV());
		v.setDt(value.getDt());
		cacheValues.add(v);
		dbValues.add(v);
		
		Calendar dateBegin = Calendar.getInstance();
		dateBegin.setTimeInMillis(value.getDt());
		dateBegin = CommonService.trunc(dateBegin, TimeType.DAY);
		
		Map<String, Object> monitorData = stationMonitorCacheService.createStationMonitorData(
				dateBegin.getTimeInMillis(), station);
		monitorData.put(StationMonitorColumn.power.getName(), value.getDv());
		
		//更新电站功率曲线
		List<PointValueDto> values = pointValueCacheService.updatePointValueList(value, dateBegin);
		
		calcDayMaxPower(station, dateBegin, values, monitorData, cacheValues, dbValues);
		
		//新监盘中心缓存
		String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(station.getCalcChannel());
		try {
			kafkaService.sendMonitor(monitorCenterTopic, monitorData, debug);
		} catch (Exception e) {
			logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(value), e);
		}
	}

	private void calcDayMaxPower(Station station, Calendar dateBegin, List<PointValueDto> values, 
			Map<String, Object> monitorData, List<PointValueDto> cacheValues, List<PointValueDto> dbValues) {
		Double max = null;
		Long maxTime = null;
		for (PointValueDto v : values) {
			if (max == null || max < v.getDv()) {
				max = v.getDv();
				maxTime = v.getDt();
			}
		}
		
		monitorData.put(StationMonitorColumn.max_power.getName(), max);
		monitorData.put(StationMonitorColumn.max_power_time.getName(), maxTime);
		
		PointInfoDto maxPTimePoint = pointCacheService.getPointByObjIdCateId(station.getPsrId(), 
				PointConstants.CATE_ID_STATION_MAXPOWERTIME_D);
		if (maxPTimePoint == null) {
			logger.error("max powe time point not exists for psrId {}", station.getPsrId());
		} else {
			PointValueDto maxV = null;
			try {
				maxV = pointValueCacheService.getPointCurrValueByPointId(station.getCalcChannel(), 
						maxPTimePoint.getPointId());
			} catch (Exception e) {
				logger.error("get max power time failed! {}", maxPTimePoint.getPointId());
			}
			if (maxV == null || maxV.getDv() != 1.0 * maxTime) {
				maxV = new PointValueDto();
				maxV.setPid(maxPTimePoint.getPointId());
				maxV.setV(maxTime.toString());
				maxV.setDt(maxTime);
				cacheValues.add(maxV);
				dbValues.add(maxV);
			}
		}
		
		PointInfoDto maxPPoint = pointCacheService.getPointByObjIdCateId(station.getPsrId(), 
				PointConstants.CATE_ID_STATION_MAXPOWER_D);
		if (maxPPoint == null) {
			logger.error("max powe point not exists for psrId {}", station.getPsrId());
		} else {
			PointValueDto maxV = null;
			try {
				maxV = pointValueCacheService.getPointCurrValueByPointId(station.getCalcChannel(), 
						maxPPoint.getPointId());
			} catch (Exception e) {
				logger.error("get max power failed! {}", maxPPoint.getPointId());
			}
			if (maxV == null || maxV.getDv() != max) {
				maxV = new PointValueDto();
				maxV.setPid(maxPPoint.getPointId());
				maxV.setV(max.toString());
				maxV.setDt(maxTime);
				cacheValues.add(maxV);
				dbValues.add(maxV);
			}
		}
	}

}
