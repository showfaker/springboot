package com.cie.nems.topology.cache.point.value;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.cie.nems.common.exception.NemsException;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.distribute.ly.LastValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;

public interface PointValueCacheService {

	public Map<Long, LastValueDto> getPointLastValueMap();
	public LastValueDto getLastValueByPid(Long pid);
	public void updateLastValue(Long pid, Long dt, String v);

	public Map<Long, String> getPointUpdateTime(List<Long> pointIds);

	public void getPointPreValues(Integer channel, List<PointValueDto> datas) throws Exception;
	public PointValueDto getPointCurrValueByPointId(Integer channel, Long pointId) throws Exception;
	public PointValueDto getPointCurrValueByPointId(Integer channel, PointInfoDto point) throws Exception;
	public Map<Long, PointValueDto> getPointCurrValuesByPointIds(Integer channel, List<Long> pointIds) throws Exception;
	public Map<Long, PointValueDto> getPointCurrValuesByPoints(Integer channel, List<PointInfoDto> points) throws Exception;
	
	public void updatePointCurrValues(Integer channel, List<PointValueDto> datas) throws Exception;
	
	public String serializer(SensorData dto) throws NemsException;
	public String serializer(PointValueDto dto) throws NemsException;
	public PointValueDto deserializer(String value) throws Exception;
	public List<PointValueDto> parseMessage(List<ConsumerRecord<Integer, String>> msgs);
	
	public List<PointValueDto> getPointValuesList(Integer channelId, PointInfoDto point, Calendar cycleBegin);
	public List<PointValueDto> updatePointValueList(PointValueDto data, Calendar dateBegin);
	public void updatePointValuesList(Integer channelId, PointInfoDto point, Calendar cycleBegin, 
			List<PointValueDto> values, Boolean setExpire);

	public boolean isValid(Integer q);
	public boolean isValid(PointValueDto v);
	public void setQByTime(PointValueDto v, String dataPeriod);
	public void setQByTime(PointValueDto v, String dataPeriod, Date now);

}
