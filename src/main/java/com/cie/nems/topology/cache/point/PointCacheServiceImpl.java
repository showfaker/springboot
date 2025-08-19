package com.cie.nems.topology.cache.point;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.device.DeviceService;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.PointInfoService;
import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.topology.cache.CountDto;
import com.cie.nems.topology.cache.dataTime.DataTimeCacheService;
import com.cie.nems.topology.cache.device.DeviceCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PointCacheServiceImpl implements PointCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.point-value:#{false}}")
	private boolean debug;

	/**测点数据格式：1只支持数字，2支持字符等其他格式*/
	@Value("${cie.point.point-value-format:#{1}}")
	private int pointValueFormat;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private PointInfoService pointInfoService;

	@Autowired
	private DataTimeCacheService timeService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	/** Map(pointId, Point) */
	private Map<Long, PointInfoDto> pointIdMap = new ConcurrentHashMap<Long, PointInfoDto>();

	/** Map(objId, Map(cateId, Point)) */
	private Map<String, Map<String, PointInfoDto>> objCateIdMap = new ConcurrentHashMap<String, Map<String, PointInfoDto>>();

	/** 
	 * Map(pointId, ExpressionDtos)</br>
	 * 被引用的测点与计算公式关联关系
	 */
	private Map<Long, List<ExpressionDto>> refPointExpressionMap = new ConcurrentHashMap<Long, List<ExpressionDto>>();

	/**
	 * Map(pointId, ExpressionDto)</br>
	 * 计算结果测点与计算公式关联关系
	 */
	private Map<Long, ExpressionDto> pointExpressionMap = new ConcurrentHashMap<Long, ExpressionDto>();
	
	/**
	 * Map(pointId, ZcPsrId)
	 * 根据逆变器/汇流箱的组串电流测点查找对应的组串设备的PSR_ID
	 */
	private Map<Long, String> pointPvStringMap = new ConcurrentHashMap<Long, String>();

	private static ObjectMapper om = new ObjectMapper();

	@Override
	public Map<Long, PointInfoDto> getPointIdMap() {
		return pointIdMap;
	}

	@Override
	public PointInfoDto getPointByPointId(Long pointId) {
		return pointIdMap.get(pointId);
	}

	@Override
	public Integer getPointChannelByPointId(Long pointId) {
		PointInfoDto point = pointIdMap.get(pointId);
		if (point != null) return point.getCalcChannel();
		return null;
	}

	@Override
	public void updatePointIdMap(List<PointInfoDto> points) {
		if (CommonService.isEmpty(points)) return;
		for (PointInfoDto p : points) {
			pointIdMap.put(p.getPointId(), p);
		}
	}

	@Override
	public void deletePointIdMap(List<Long> pointIds) {
		if (CommonService.isEmpty(pointIds)) return;
		for (Long pid : pointIds) {
			pointIdMap.remove(pid);
		}
	}

	@Override
	public Map<String, Map<String, PointInfoDto>> getObjIdCateIdMap() {
		return objCateIdMap;
	}

	@Override
	public Map<String, PointInfoDto> getPointsByObjId(String objId) {
		return objCateIdMap.get(objId);
	}

	@Override
	public PointInfoDto getPointByObjIdCateId(String objId, String cateId) {
		Map<String, PointInfoDto> map = objCateIdMap.get(objId);
		if (map == null) return null;
		return map.get(cateId);
	}

	@Override
	public ExpressionDto getPointExpressions(Long pointId) {
		return pointExpressionMap.get(pointId);
	}

	@Override
	public List<ExpressionDto> getRefPointExpressions(Long pointId) {
		return refPointExpressionMap.get(pointId);
	}

	@Override
	public int deleteExpression(Long pointId) {
		ExpressionDto exp = pointExpressionMap.remove(pointId);
		return exp == null ? 0 : 1;
	}

	@Override
	public int deleteRefExpressions(Long pointId) {
		List<ExpressionDto> expressions = refPointExpressionMap.remove(pointId);
		return expressions == null ? 0 : expressions.size();
	}

	@Override
	public void updatePointCache(List<PointInfoDto> points, CountDto count) {
		if (CommonService.isEmpty(points)) return;
		for (PointInfoDto p : points) {
			Map<String, PointInfoDto> map = objCateIdMap.get(p.getPsrId());
			if (map == null) {
				map = new ConcurrentHashMap<String, PointInfoDto>();
				objCateIdMap.put(p.getPsrId(), map);
			}
			map.put(p.getCateId(), p);
			
			if (StringUtils.isNotEmpty(p.getCalcFormula()) && p.getCalcFormula().startsWith("{")) {
				try {
					ExpressionDto exp = om.readValue(p.getCalcFormula(), new TypeReference<ExpressionDto>() {});
					//ExpressionDto exp = CommonService.readValue();
					if (exp != null) {
						exp.setOwnPointId(p.getPointId());
						pointExpressionMap.put(p.getPointId(), exp);
						
						count.setExpCount(count.getExpCount() + 1);
						count.addExpAll(1);
						
						updatePointExpressionMap(exp, exp.getPoints1());
						updatePointExpressionMap(exp, exp.getPoints2());
					}
				} catch (Exception e) {
					logger.error("set point expression cache failed! {}", p.getCalcFormula(), e);
				}
			}
		}
	}

	private void updatePointExpressionMap(ExpressionDto expression, List<Long> pointIds) {
		if (CommonService.isEmpty(pointIds)) return;
		
		for (Long pid : pointIds) {
			List<ExpressionDto> expressions = refPointExpressionMap.get(pid);
			if (expressions == null) {
				expressions = new ArrayList<ExpressionDto>(3);
				refPointExpressionMap.put(pid, expressions);
			}
			expressions.add(expression);
		}
	}

	@Override
	public void deleteObjIdCateIdMap(String psrId) {
		objCateIdMap.remove(psrId);
	}

	@Override
	public void deleteObjIdCateIdMap(String psrId, String cateId) {
		Map<String, PointInfoDto> map = objCateIdMap.get(psrId);
		if (map != null) {
			map.remove(cateId);
		}
	}

	@Override
	public int initDistrCache(List<Integer> channelIds) {
		int deviceCount = 0, pointCount = 0;
		
		long t1 = System.currentTimeMillis();
		
		Page<String> psrIds = deviceService.getPsrIds(channelIds, 
				PageRequest.of(0, DeviceCacheService.deviceBatchNumber, Sort.by(Direction.ASC, "psr_id")));
		deviceCount += psrIds.getNumberOfElements();
		List<PointInfoDto> points = pointInfoService.getDevicePoints(psrIds.getContent(), false);
		pointCount += points == null ? 0 : points.size();
		updatePointIdMap(points);
		logger.debug("load {}/{} points for {}/{} devices", points == null ? 0 : points.size(), 
				pointCount, deviceCount, psrIds.getTotalElements());
		
		for (int i=1; i<psrIds.getTotalPages(); ++i) {
			psrIds = deviceService.getPsrIds(channelIds, 
					PageRequest.of(i, DeviceCacheService.deviceBatchNumber, Sort.by(Direction.ASC, "psr_id")));
			deviceCount += psrIds.getNumberOfElements();
			points = pointInfoService.getDevicePoints(psrIds.getContent(), false);
			pointCount += points == null ? 0 : points.size();
			updatePointIdMap(points);
			logger.debug("load {}/{} points for {}/{} devices", points == null ? 0 : points.size(), 
					pointCount, deviceCount, psrIds.getTotalElements());
		}
		
		long t2 = System.currentTimeMillis();
		logger.debug("used {} seconds", (1.0 * t2 - t1) / 1000L);
		
		return pointCount;
	}

	@Override
	public void initStationPoints(List<Integer> channelIds, CountDto count) {
		List<PointInfoDto> points = pointInfoService.getStationPoints(channelIds);
		count.setPointCount(points == null ? 0 : points.size());
		count.addPointAll(count.getPointCount());
		
		updatePointIdMap(points);
		updatePointCache(points, count);
		logger.debug("load {}/{} points, {}/{} expressions for stations", count.getPointCount(), 
				count.getPointAll(), count.getExpCount(), pointExpressionMap.size());
	}

	@Override
	public void initDevicePoints(List<Device> devices, List<String> psrIds, CountDto count) {
		timeService.initDeviceUpdateTime(devices);
		
		List<PointInfoDto> points = pointInfoService.getDevicePoints(psrIds, true);
		count.setPointCount(points == null ? 0 : points.size());
		count.addPointAll(count.getPointCount());
		count.setExpCount(0);
		
		updatePointIdMap(points);
		updatePointCache(points, count);
	}

	@Override
	public Map<String, Integer> getPointCacheSize() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("pointIdMap", pointIdMap.size());
		map.put("objCateIdMap", objCateIdMap.size());
		map.put("pointDtMap", pointValueCacheService.getPointLastValueMap().size());
		return map;
	}

	@Override
	public Map<Long, PointInfoDto> getPointsByPointIds(List<Long> pointIds) {
		Map<Long, PointInfoDto> map = new HashMap<Long, PointInfoDto>();
		for (Long pid : pointIds) {
			map.put(pid, pointIdMap.get(pid));
		}
		return map;
	}

	@Override
	public List<PointInfoDto> getPoints(String psrId, Set<String> cateIds, Set<String> sysCateIds) {
		Map<String, PointInfoDto> objPoints = objCateIdMap.get(psrId);
		if (CommonService.isEmpty(cateIds) || CommonService.isEmpty(sysCateIds)) {
			return new ArrayList<PointInfoDto>(objPoints.values());
		} else {
			List<PointInfoDto> list = new ArrayList<PointInfoDto>();
			for (PointInfoDto p : objPoints.values()) {
				if (cateIds != null && cateIds.contains(p.getCateId())) {
					list.add(p);
				} else if (sysCateIds != null && sysCateIds.contains(p.getSysCateId())) {
					list.add(p);
				}
			}
			return list;
		}
	}

	@Override
	public Map<Long, String> getPointPvStringMap() {
		return pointPvStringMap;
	}

	@Override
	public String getPointPvStringPsrId(Long pointId) {
		if (CommonService.isEmpty(pointId)) return null;
		return pointPvStringMap.get(pointId);
	}

	public void updatePointPvStringCache(Map<Long, String> pointPvStringMap) {
		this.pointPvStringMap = pointPvStringMap;
	}

	
}
