package com.cie.nems.topology.calc.station.algorithm;

import java.util.Calendar;
import java.util.Date;
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
import com.cie.nems.monitor.station.StationMonitorColumn;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.station.Station;
import com.cie.nems.topology.cache.monitor.station.StationMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.station.StationCacheService;
import com.cie.nems.topology.calc.expression.CumulateService;

@Service
public class IrradiationCalcServiceImpl implements IrradiationCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.station-calc:#{false}}")
	private boolean debug;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private CumulateService cumulateService;

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private KafkaService kafkaService;

	@Override
	public void calc(PointValueDto data, List<PointValueDto> cacheValues, List<PointValueDto> dbValues) {
		Calendar dateBegin = Calendar.getInstance();
		dateBegin.setTimeInMillis(data.getDt());
		dateBegin = CommonService.trunc(dateBegin, TimeType.DAY);
		
		//更新并获得当日累计辐照量曲线
		List<PointValueDto> values = pointValueCacheService.updatePointValueList(data, dateBegin);
		
		if (CommonService.isEmpty(values)) {
			logger.debug("values for pid {} and time {}({}) is empty", data.getPid(), dateBegin.getTimeInMillis(),
					(dateBegin.get(Calendar.YEAR) * 10000 + dateBegin.get(Calendar.MONTH) * 100 + 100 + dateBegin.get(Calendar.DAY_OF_MONTH)));
			return;
		}
		
		//用累积量增量算法将非法值剔除
		values = cumulateService.cumulate(values, null);
		
		Date todayBegin = CommonService.trunc(new Date(), TimeType.DAY);
		
		PointInfoDto hourPoint = pointCacheService.getPointByObjIdCateId(data.getPoint().getPsrId(), 
				PointConstants.CATE_ID_AI_DZ_QXFZL_H);
		if (hourPoint == null) {
			logger.error("point not exists! objId: {}, cateId: {}", data.getPoint().getPsrId(), PointConstants.CATE_ID_AI_DZ_QXFZL_H);
		} else {
			cumulateService.calcHourValues(values, hourPoint, todayBegin, cacheValues, dbValues);
		}
		
		PointInfoDto dayPoint = pointCacheService.getPointByObjIdCateId(data.getPoint().getPsrId(), 
				PointConstants.CATE_ID_AI_DZ_QXFZL_D);
		if (dayPoint == null) {
			logger.error("point not exists! objId: {}, cateId: {}", data.getPoint().getPsrId(), PointConstants.CATE_ID_AI_DZ_QXFZL_D);
		} else {
			PointValueDto dValue = cumulateService.calcDayValues(values, dayPoint, dateBegin, todayBegin, cacheValues, dbValues);
			
			if (dValue != null) {
				Station station = stationCacheService.getStationByStationId(data.getPoint().getStationId());
				if (station == null) {
					logger.error("stationId {} not exists", data.getPoint().getStationId());
					return;
				}
				
				Map<String, Object> monitorData = stationMonitorCacheService.createStationMonitorData(
						dateBegin.getTimeInMillis(), station);
				monitorData.put(StationMonitorColumn.irradiation.getName(), dValue.getDv());

				String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(station.getCalcChannel());
				try {
					kafkaService.sendMonitor(monitorCenterTopic, monitorData, debug);
				} catch (Exception e) {
					logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(monitorData), e);
				}
			}
		}
	}

}
