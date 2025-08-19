package com.cie.nems.topology.cache.point;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.cie.nems.device.Device;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.expression.ExpressionDto;
import com.cie.nems.topology.cache.CountDto;

public interface PointCacheService {

	public Map<Long, PointInfoDto> getPointIdMap();
	public PointInfoDto getPointByPointId(Long pointId);
	public Integer getPointChannelByPointId(Long pointId);
	public void updatePointIdMap(List<PointInfoDto> points);
	public void deletePointIdMap(List<Long> pointIds);

	public Map<String, Map<String, PointInfoDto>> getObjIdCateIdMap();
	public Map<String, PointInfoDto> getPointsByObjId(String objId);
	public PointInfoDto getPointByObjIdCateId(String objId, String cateId);	
	public void deleteObjIdCateIdMap(String psrId);
	public void deleteObjIdCateIdMap(String psrId, String cateId);

	public ExpressionDto getPointExpressions(Long pointId);
	public List<ExpressionDto> getRefPointExpressions(Long pointId);
	public int deleteExpression(Long pointId);
	public int deleteRefExpressions(Long pointId);

	public void updatePointCache(List<PointInfoDto> points, CountDto count);

	public int initDistrCache(List<Integer> channelIds);
	
	public Map<String, Integer> getPointCacheSize();
	public Map<Long, PointInfoDto> getPointsByPointIds(List<Long> pointIds);
	public List<PointInfoDto> getPoints(String psrId, Set<String> cateIds, Set<String> sysCateIds);
	
	public Map<Long, String> getPointPvStringMap();
	public String getPointPvStringPsrId(Long pointId);
	
	public void initDevicePoints(List<Device> devices, List<String> psrIds, CountDto count);
	public void initStationPoints(List<Integer> channelIds, CountDto count);

}
