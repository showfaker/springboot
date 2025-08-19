package com.cie.nems.topology.calc.device;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.calc.device.battery.BatteryCalcService;
import com.cie.nems.topology.calc.device.inverter.InverterCalcService;
import com.cie.nems.topology.calc.device.pvString.PvStringCalcService;

@Service
@Scope("prototype")
public class DeviceCalcServiceImpl implements DeviceCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.device-calc:#{false}}")
	private boolean debug;

	@Autowired
	private ExceptionService exceptionService;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private InverterCalcService inverterCalcService;

	@Autowired
	private BatteryCalcService batteryCalcService;

	@Autowired
	private PvStringCalcService pvStringCalcService;

	@Autowired
	private KafkaService kafkaService;

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) throws Exception {
		List<PointValueDto> datas = pointValueCacheService.parseMessage(msgs);

		if (CommonService.isEmpty(datas)) return;
		
		calc(datas);
	}

	private void calc(List<PointValueDto> datas) throws Exception {
		Map<Integer, List<PointValueDto>> relaValues = new HashMap<Integer, List<PointValueDto>>();
		Calendar dateBegin = CommonService.trunc(Calendar.getInstance(), TimeType.DAY);
		for (PointValueDto data : datas) {
			PointInfoDto point = pointCacheService.getPointByPointId(data.getPid());
			if (point == null) {
				logger.error("pid {} not exists", data.getPid());
				continue;
			}
			data.setPoint(point);
			
			if (PointConstants.CATE_ID_AM_NB_POWER.equals(point.getCateId())) {
				//逆变器功率
				inverterCalcService.calcInverterPower(data, relaValues, dateBegin);
			} else if (PointConstants.CATE_ID_AM_EBS_CHARGE_A.equals(point.getCateId())
					|| PointConstants.CATE_ID_AM_EBS_DISCHARGE_A.equals(point.getCateId())) {
				batteryCalcService.calcBatteryCharge(data, relaValues, dateBegin);
			}else if (PointConstants.CATE_ID_AM_HLX_PVI_INPUT.equals(point.getSysCateId())
					|| PointConstants.CATE_ID_AM_NB_PVI_INPUT.equals(point.getSysCateId())) {
				//组串电流
				pvStringCalcService.calcPvCurrent(data, relaValues, dateBegin);
			}
		}
		for (Entry<Integer, List<PointValueDto>> e : relaValues.entrySet()) {
			try {
				pointValueCacheService.updatePointCurrValues(e.getKey(), e.getValue());
			} catch (Exception ex) {
				logger.error("update point curr values failed!", ex.getMessage());
				exceptionService.log(this.getClass().getName() + "-updatePointCurrValue", "", ex);
			}
			
			String saveTopic = kafkaService.getSaveTopicName(e.getKey());
			for (PointValueDto data : e.getValue()) {
				kafkaService.sendPoint(saveTopic, data, debug);
			}
		}
	}

	private Set<String> calcCateIds = null;
	private Set<String> calcSysCateIds = null;
	@Override
	public boolean isDeviceCalcPoints(PointInfoDto point) {
		if (calcCateIds == null) {
			calcCateIds = new HashSet<String>();
			calcCateIds.add(PointConstants.CATE_ID_AM_NB_POWER);
			calcCateIds.add(PointConstants.CATE_ID_AM_EBS_CHARGE_A);
			calcCateIds.add(PointConstants.CATE_ID_AM_EBS_DISCHARGE_A);
		}
		if (calcCateIds.contains(point.getCateId())) {
			return true;
		}
		
		if (calcSysCateIds == null) {
			calcSysCateIds = new HashSet<String>();
			calcSysCateIds.add(PointConstants.CATE_ID_AM_NB_PVI_INPUT);
			calcSysCateIds.add(PointConstants.CATE_ID_AM_HLX_PVI_INPUT);
		}
		if (calcSysCateIds.contains(point.getSysCateId())) {
			return true;
		}
		return false;
	}

}
