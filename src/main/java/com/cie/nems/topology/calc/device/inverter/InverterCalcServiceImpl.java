package com.cie.nems.topology.calc.device.inverter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.device.DeviceService;
import com.cie.nems.monitor.device.DeviceMonitorColumn;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.monitor.device.DeviceMonitorCacheService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;

@Service
public class InverterCalcServiceImpl implements InverterCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.device-calc:#{false}}")
	private boolean debug;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private DeviceCacheService deviceCacheService;

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@Autowired
	private KafkaService kafkaService;

	@Override
	public void calcInverterPower(PointValueDto data, Map<Integer, List<PointValueDto>> relaValues, 
			Calendar dateBegin) {
		//只处理当日数据，历史数据在pre中入库即可
		if (data.getDt() < dateBegin.getTimeInMillis()) return;
		
		Device d = deviceCacheService.getDeviceByDeviceId(data.getPoint().getDeviceId());
		if (d == null) {
			logger.error("device not exists {}", data.getPoint().getDeviceId());
			return;
		}
		Map<String, Object> monitorData = deviceMonitorCacheService.createDeviceMonitorData(
				dateBegin.getTimeInMillis(), d);
		
		//更新逆变器运行状态
		calcRunStatus(data, dateBegin, monitorData);
		
		//更新功率曲线
		List<PointValueDto> values = pointValueCacheService.updatePointValueList(data, dateBegin);
		
		//计算日最大功率
		calcDayMaxPower(data, dateBegin, values, monitorData, relaValues);
		
		//计算日开关机时间与运行时长
		calcRunTimes(data, dateBegin, values, monitorData);
		
		//新监盘中心缓存
		String monitorCenterTopic = kafkaService.getMonitorCenterTopicName(d.getCalcChannel());
		try {
			kafkaService.sendMonitor(monitorCenterTopic, monitorData, debug);
		} catch (Exception e) {
			logger.error("send to {} : {} failed!", monitorCenterTopic, CommonService.toString(data), e);
		}
		
		//向后传递计算电站功率
		String stationCalcTopic = kafkaService.getStationCalcTopicName(d.getCalcChannel());
		try {
			kafkaService.sendPoint(stationCalcTopic, data, debug);
		} catch (Exception e) {
			logger.error("send to {} : {} failed!", stationCalcTopic, CommonService.toString(data), e);
		}
	}

	private void calcRunStatus(PointValueDto data, Calendar dateBegin, Map<String, Object> monitorData) {
		String runStatus = isRun(data.getDv()) ? DeviceService.DEVICE_RUN_STATUS_RUNNING : DeviceService.DEVICE_RUN_STATUS_STOP;
		monitorData.put(DeviceMonitorColumn.run_status.getName(), runStatus);
		deviceMonitorCacheService.setRunStatus(monitorData, runStatus);
	}

	private boolean isRun(Double dv) {
		return dv > ZERO_POWER_VALUE || dv < -1 * ZERO_POWER_VALUE;
	}

	private void calcDayMaxPower(PointValueDto data, Calendar dateBegin, List<PointValueDto> values, 
			Map<String, Object> monitorData, Map<Integer, List<PointValueDto>> relaValues) {
		Double max = null;
		Long maxTime = null;
		for (PointValueDto v : values) {
			if (max == null || max < v.getDv()) {
				max = v.getDv();
				maxTime= v.getDt();
			}
		}
		
		monitorData.put(DeviceMonitorColumn.value12.getName(), max);
		
		PointInfoDto maxPTimePoint = pointCacheService.getPointByObjIdCateId(data.getPoint().getPsrId(), 
				PointConstants.CATE_ID_AI_NB_MAXP_TIME_D);
		if (maxPTimePoint == null) {
			logger.error("max powe time point not exists for psrId {}", data.getPoint().getPsrId());
		} else {
			PointValueDto maxV = null;
			try {
				maxV = pointValueCacheService.getPointCurrValueByPointId(data.getPoint().getCalcChannel(), 
						maxPTimePoint.getPointId());
			} catch (Exception e) {
				logger.error("get max power time failed! {}", maxPTimePoint.getPointId());
			}
			if (maxV == null || maxV.getDv() != 1.0 * maxTime) {
				maxV = new PointValueDto();
				maxV.setPid(maxPTimePoint.getPointId());
				maxV.setV(maxTime.toString());
				maxV.setDt(data.getDt());
				List<PointValueDto> relaList = relaValues.get(maxPTimePoint.getCalcChannel());
				if (relaList == null) {
					relaList = new ArrayList<PointValueDto>();
					relaValues.put(maxPTimePoint.getCalcChannel(), relaList);
				}
				relaList.add(maxV);
			}
		}
		
		PointInfoDto maxPPoint = pointCacheService.getPointByObjIdCateId(data.getPoint().getPsrId(), 
				PointConstants.CATE_ID_AI_NB_MAXP_D);
		if (maxPPoint == null) {
			logger.error("max powe point not exists for psrId {}", data.getPoint().getPsrId());
		} else {
			PointValueDto maxV = null;
			try {
				maxV = pointValueCacheService.getPointCurrValueByPointId(data.getPoint().getCalcChannel(), 
						maxPPoint.getPointId());
			} catch (Exception e) {
				logger.error("get max power failed! {}", maxPPoint.getPointId());
			}
			if (maxV == null || maxV.getDv() != max) {
				maxV = new PointValueDto();
				maxV.setPid(maxPPoint.getPointId());
				maxV.setV(max.toString());
				maxV.setDt(data.getDt());
				List<PointValueDto> relaList = relaValues.get(maxPPoint.getCalcChannel());
				if (relaList == null) {
					relaList = new ArrayList<PointValueDto>();
					relaValues.put(maxPPoint.getCalcChannel(), relaList);
				}
				relaList.add(maxV);
			}
		}
	}

	private void calcRunTimes(PointValueDto data, Calendar dateBegin, List<PointValueDto> values, 
			Map<String, Object> monitorData) {
		Integer runStartTime = null, runEndTime = null, runTimes = null;
		
		for (PointValueDto v : values) {
			if (isRun(v.getDv())) {
				//第一次功率大于0
				runStartTime = (int) ((v.getDt() - dateBegin.getTimeInMillis()) / 1000L);
				break;
			}
		}
		for (int i=values.size()-1; i>=0; --i) {
			//从最后一个功率值往前倒着检查：
			if (isRun(values.get(i).getDv())) {
				//找到倒数第一个大于0的功率点
				if (i < values.size() - 1 && !isRun(values.get(i + 1).getDv())) {
					//如果是最后一次从非0变为0，则停机时间就是此刻
					runEndTime = (int) ((values.get(i + 1).getDt() - dateBegin.getTimeInMillis()) / 1000L);
					break;
				} else {
					//否则，说明还在运行
					break;
				}
			}
		}
		if (runStartTime != null && runEndTime != null) {
			runTimes = runEndTime - runStartTime;
		}
		
		monitorData.put(DeviceMonitorColumn.run_start_time.getName(), runStartTime);
		monitorData.put(DeviceMonitorColumn.run_end_time.getName(), runEndTime);
		monitorData.put(DeviceMonitorColumn.run_times.getName(), runTimes);
	}

}
