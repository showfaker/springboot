package com.cie.nems.topology.save;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.save.SaveCacheService;
import com.cie.nems.topology.save.mongo.MongoSaveService;
import com.cie.nems.topology.save.postgre.PostgreSaveService;

@Service
@Scope("prototype")
public class SaveServiceImpl implements SaveService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.save:#{false}}")
	private boolean debug;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private MongoSaveService mongoSaveService;

	@Autowired
	private PostgreSaveService postgreSaveService;

	@Autowired
	private SaveCacheService saveCacheService;

	@Override
	public void execute(List<ConsumerRecord<Integer, String>> msgs) {
		List<PointValueDto> datas = pointValueCacheService.parseMessage(msgs);

		if (CommonService.isEmpty(datas)) return;
		
		save(datas);
	}

	/**
	 * 获取测点档案，并按通道分开处理
	 */
	private void save(List<PointValueDto> datas) {
		Map<String, List<PointValueDto>> dbDatas = new HashMap<String, List<PointValueDto>>();
		String db = null;
		for (PointValueDto data : datas) {
			PointInfoDto point = pointCacheService.getPointByPointId(data.getPid());
			if (point == null) {
				logger.error("pid {} not exists", data.getPid());
				continue;
			}
			
			data.setPoint(point);
			
			if (saveCheck(data)) {
				db = point.getDbs() == null ? PointConstants.POINT_VALUE_DATABASE_MONGODB : point.getDbs();
				List<PointValueDto> list = dbDatas.get(db);
				if (list == null) {
					list = new ArrayList<PointValueDto>();
					dbDatas.put(db, list);
				}
				list.add(data);
			}
		}
		
		for (Entry<String, List<PointValueDto>> e : dbDatas.entrySet()) {
			if (PointConstants.POINT_VALUE_DATABASE_MONGODB.equals(e.getKey())) {
				mongoSaveService.save(datas);
			} else if (PointConstants.POINT_VALUE_DATABASE_POSTGRE.equals(e.getKey())) {
				postgreSaveService.save(datas);
			} else {
				logger.error("unknow db: {}", e.getKey());
			}
		}
	}

	private boolean saveCheck(PointValueDto data) {
		if (PointConstants.POINT_DATA_PERIOD_ONCHANGE.equals(data.getPoint().getDataPeriod())) {
			Double preDv = saveCacheService.getPreSaveDv(data.getPid());
			if (preDv == null || preDv.compareTo(data.getDv()) != 0) return true;
		} else if (PointConstants.POINT_DATA_PERIOD_1MIN.equals(data.getPoint().getDataPeriod())) {
			Long preDt = saveCacheService.getPreSaveDt(data.getPid());
			if (preDt == null || Math.abs(preDt - data.getDt()) > 60000L) return true;
		} else if (PointConstants.POINT_DATA_PERIOD_5MIN.equals(data.getPoint().getDataPeriod())) {
			Long preDt = saveCacheService.getPreSaveDt(data.getPid());
			if (preDt == null || Math.abs(preDt - data.getDt()) > 300000L) return true;
		} else if (PointConstants.POINT_DATA_PERIOD_10MIN.equals(data.getPoint().getDataPeriod())) {
			Long preDt = saveCacheService.getPreSaveDt(data.getPid());
			if (preDt == null || Math.abs(preDt - data.getDt()) > 600000L) return true;
		} else if (PointConstants.POINT_DATA_PERIOD_15MIN.equals(data.getPoint().getDataPeriod())) {
			Long preDt = saveCacheService.getPreSaveDt(data.getPid());
			if (preDt == null || Math.abs(preDt - data.getDt()) > 900000L) return true;
		} else if (PointConstants.POINT_DATA_PERIOD_HOUR.equals(data.getPoint().getDataPeriod())
				|| PointConstants.POINT_DATA_PERIOD_DAY.equals(data.getPoint().getDataPeriod())
				|| PointConstants.POINT_DATA_PERIOD_MONTH.equals(data.getPoint().getDataPeriod())
				|| PointConstants.POINT_DATA_PERIOD_YEAR.equals(data.getPoint().getDataPeriod())) {
			Long preDt = saveCacheService.getPreSaveDt(data.getPid());
			if (preDt == null || Math.abs(preDt - data.getDt()) > 300000L) return true;
		} else if (PointConstants.POINT_DATA_PERIOD_NOTSAVE.equals(data.getPoint().getDataPeriod())) {
			return false;
		}
		return false;
	}

	@Override
	public void saveYxDayData(Date date) {
		// TODO Auto-generated method stub
		
	}

}
