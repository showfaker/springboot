package com.cie.nems.topology.calc.station;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

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
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.calc.station.algorithm.IrradiationCalcService;
import com.cie.nems.topology.calc.station.algorithm.StationPowerCalcService;
import com.cie.nems.topology.calc.station.algorithm.StationRunTimesCalcService;

@Service
@Scope("prototype")
public class StationCalcServiceImpl implements StationCalcService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.station-calc:#{false}}")
	private boolean debug;

	@Autowired
	private ExceptionService exceptionService;
	
	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private StationPowerCalcService powerCalcService;

	@Autowired
	private StationRunTimesCalcService runTimesCalcService;

	@Autowired
	private IrradiationCalcService irraCalcService;

	@Autowired
	private KafkaService kafkaService;

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) throws Exception {
		List<PointValueDto> datas = pointValueCacheService.parseMessage(msgs);

		if (CommonService.isEmpty(datas)) return;
		
		calc(datas);
	}

	private void calc(List<PointValueDto> datas) throws Exception {
		Map<Integer, List<PointValueDto>> cacheValueMap = new HashMap<Integer, List<PointValueDto>>();
		Map<Integer, List<PointValueDto>> dbValueMap = new HashMap<Integer, List<PointValueDto>>();
		for (PointValueDto data : datas) {
			PointInfoDto point = pointCacheService.getPointByPointId(data.getPid());
			if (point == null) {
				logger.error("pid {} not exists", data.getPid());
				continue;
			}
			data.setPoint(point);
			
			List<PointValueDto> cacheValues = cacheValueMap.get(point.getCalcChannel());
			if (cacheValues == null) {
				cacheValues = new ArrayList<PointValueDto>();
				cacheValueMap.put(point.getCalcChannel(), cacheValues);
			}
			List<PointValueDto> dbValues = dbValueMap.get(point.getCalcChannel());
			if (dbValues == null) {
				dbValues = new ArrayList<PointValueDto>();
				dbValueMap.put(point.getCalcChannel(), dbValues);
			}
			
			if (PointConstants.CATE_ID_AM_NB_POWER.equals(data.getPoint().getCateId())
			 || PointConstants.CATE_ID_AM_DNB_ZXYGZGL.equals(data.getPoint().getCateId())
			 || PointConstants.CATE_ID_AM_DNB_FXYGZGL.equals(data.getPoint().getCateId())) {
				powerCalcService.calc(data, cacheValues, dbValues);
			} else if (PointConstants.CATE_ID_AI_DZ_QXFZL_A.equals(data.getPoint().getCateId())) {
				irraCalcService.calc(data, cacheValues, dbValues);
			}
		}

		for (Entry<Integer, List<PointValueDto>> e : cacheValueMap.entrySet()) {
			try {
				pointValueCacheService.updatePointCurrValues(e.getKey(), e.getValue());
			} catch (Exception ex) {
				logger.error("update point curr values failed!", ex.getMessage());
				exceptionService.log(this.getClass().getName() + "-updatePointCurrValue", "", ex);
			}
		}
		for (Entry<Integer, List<PointValueDto>> e : dbValueMap.entrySet()) {
			String saveTopic = kafkaService.getSaveTopicName(e.getKey());
			for (PointValueDto data : e.getValue()) {
				kafkaService.sendPoint(saveTopic, data, debug);
			}
		}
	}

	private Set<String> calcCateIds = null;
	private Set<String> calcSysCateIds = null;
	@Override
	public boolean isStationCalcPoints(PointInfoDto point) {
		if (calcCateIds == null) {
			calcCateIds = new HashSet<String>();
			calcCateIds.add(PointConstants.CATE_ID_AI_DZ_QXFZL_A);
		}
		if (calcCateIds.contains(point.getCateId())) {
			return true;
		}
		
		if (calcSysCateIds == null) {
			calcSysCateIds = new HashSet<String>();
		}
		if (calcSysCateIds.contains(point.getSysCateId())) {
			return true;
		}
		return false;
	}

	@Override
	public void calc(Date now) {
		runTimesCalcService.calc(now);
		//TODO
	}

}
