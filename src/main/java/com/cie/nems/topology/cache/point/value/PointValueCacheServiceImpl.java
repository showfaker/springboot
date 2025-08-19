package com.cie.nems.topology.cache.point.value;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.CacheConstants;
import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.common.exception.NemsException;
import com.cie.nems.common.redis.RedisService;
import com.cie.nems.common.redis.RedisService.Data;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.service.CommonService.TimeType;
import com.cie.nems.point.PointConstants;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.topology.distribute.ly.LastValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;

@Service
public class PointValueCacheServiceImpl implements PointValueCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.point-value:#{false}}")
	private boolean debug;

	/**测点数据格式：1只支持数字，2支持字符等其他格式*/
	@Value("${cie.point.point-value-format:#{1}}")
	private int pointValueFormat;

	@Autowired
	private RedisService redisService;

	@Autowired
	private ExceptionService exceptionService;

	/**
	 * Map(pid, {v,dt})</br>
	 * 存放distribute从接口收到的最新值，用于过滤过于密集的上报数据</br>
	 * 此缓存不能用于后续拓扑计算
	 */
	private Map<Long, LastValueDto> pointLastValueMap = new ConcurrentHashMap<Long, LastValueDto>();

	@Override
	public Map<Long, LastValueDto> getPointLastValueMap() {
		return pointLastValueMap;
	}
	
	@Override
	public LastValueDto getLastValueByPid(Long pid) {
		return pointLastValueMap.get(pid);
	}

	@Override
	public void updateLastValue(Long pid, Long dt, String v) {
		pointLastValueMap.put(pid, new LastValueDto(dt, v));
	}

	@Override
	public Map<Long, String> getPointUpdateTime(List<Long> pointIds) {
		Map<Long, String> result = new HashMap<Long, String>();
		if (CommonService.isEmpty(pointIds)) return result;
		SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		LastValueDto last = null;
		for (Long pid : pointIds) {
			last = pointLastValueMap.get(pid);
			if (last == null) {
				result.put(pid, null);
			} else {
				result.put(pid, "{v: " + last.getV() + ", dt: " + 
						(last.getDt() == null ? "null" : fmt.format(new Date(last.getDt())) + "}"));
			}
		}
		return result;
	}

	@Override
	public void getPointPreValues(Integer channel, List<PointValueDto> datas) throws Exception {
		if (CommonService.isEmpty(datas)) return;
		
		List<Long> pointIds = new ArrayList<Long>(datas.size());
		List<String> strPointIds = new ArrayList<String>(datas.size());
		for (int i=0; i<datas.size(); ++i) {
			PointValueDto d = datas.get(i);
			if (i > 0) {
				PointValueDto pre = null;
				for (int j=i-1; j>=0; --j) {
					/* 
					 * 如果一个批次的消息中有同一个测点的多个测点值，则不能简单的用Redis缓存内的数据作为其前值
					 * 而是该测点的第一个测点值的前值从redis取， 之后的测点值用其上一个测点值为前值
					 */
					if (datas.get(j).getPid().equals(d.getPid())) {
						pre = datas.get(j);
						break;
					}
				}
				if (pre != null) {
					d.setPreValue(pre);
				}
			}
			
			pointIds.add(d.getPid());
			strPointIds.add(String.valueOf(d.getPid()));
		}
		Map<Long, PointValueDto> preValues = getPointCurrValues(channel, pointIds, strPointIds);
		for (PointValueDto d : datas) {
			if (d.getPreValue() == null) {
				d.setPreValue(preValues.get(d.getPid()));
			}
		}
	}

	@Override
	public PointValueDto getPointCurrValueByPointId(Integer channel, Long pointId) throws Exception {
		Map<Long, PointValueDto> values = getPointCurrValues(channel, Arrays.asList(pointId), 
				Arrays.asList(String.valueOf(pointId)));
		return values.get(pointId);
	}

	@Override
	public PointValueDto getPointCurrValueByPointId(Integer channel, PointInfoDto point) throws Exception {
		Map<Long, PointValueDto> values = getPointCurrValues(channel, Arrays.asList(point.getPointId()), 
				Arrays.asList(String.valueOf(point.getPointId())));
		return values.get(point.getPointId());
	}

	@Override
	public Map<Long, PointValueDto> getPointCurrValuesByPoints(Integer channel, List<PointInfoDto> points) throws Exception {
		if (CommonService.isEmpty(points)) {
			return new HashMap<Long, PointValueDto>(0);
		}
		
		List<Long> pointIds = new ArrayList<Long>(points.size());
		List<String> strPointIds = new ArrayList<String>(points.size());
		for (PointInfoDto p : points) {
			pointIds.add(p.getPointId());
			strPointIds.add(String.valueOf(p.getPointId()));
		}
		return getPointCurrValues(channel, pointIds, strPointIds);
	}
	
	@Override
	public Map<Long, PointValueDto> getPointCurrValuesByPointIds(Integer channel, List<Long> pointIds) throws Exception {
		if (CommonService.isEmpty(pointIds)) {
			return new HashMap<Long, PointValueDto>(0);
		}
		
		List<String> strPointIds = new ArrayList<String>(pointIds.size());
		for (Long pid : pointIds) {
			strPointIds.add(String.valueOf(pid));
		}
		return getPointCurrValues(channel, pointIds, strPointIds);
	}
	
	private Map<Long, PointValueDto> getPointCurrValues(Integer channel, List<Long> pointIds, 
			List<String> strPointIds) throws Exception {
		List<String> values = redisService.hmget(Data.POINT_CURR_VALUE, channel, 
				CacheConstants.CACHE_POINT_CURR_VALUE, strPointIds);
		
		Map<Long, PointValueDto> pointValues = new HashMap<Long, PointValueDto>();
		for (int i=0; i<pointIds.size(); ++i) {
			pointValues.put(pointIds.get(i), deserializer(values.get(i)));
		}
		return pointValues;
	}

	@Override
	public void updatePointCurrValues(Integer channel, List<PointValueDto> datas) throws Exception {
		if (CommonService.isEmpty(datas)) return;
		
		Map<String, String> values = new HashMap<String, String>(datas.size());
		for (PointValueDto d : datas) {
			if (d.getPreValue() != null && d.getPreValue().getDt() != null) {
				if (d.getPreValue().getDt() > d.getDt()) {
					//未按时间顺序送到的数据直接忽略
					if (debug) logger.debug("ignore value, pid:{}, preDt: {}, dt: {}", d.getPid(),
							d.getPreValue().getDt(), d.getDt());
					continue;
				} else if (d.getPreValue().getDt().equals(d.getDt())) {
					//相同时间的数据，看值有没有变化
					if (StringUtils.equals(d.getPreValue().getV(), d.getV())) {
						if (debug) logger.debug("ignore value, pid:{}, dt: {}, preV: {}, v: {}", d.getPid(),
								d.getDt(), d.getPreValue().getV(), d.getV());
						continue;
					}
				}
			}

			values.put(String.valueOf(d.getPid()), serializer(d));
		}
		
		redisService.hmset(Data.POINT_CURR_VALUE, channel, CacheConstants.CACHE_POINT_CURR_VALUE, values);
		if (debug) {
			logger.debug("hmset {} : {}", CacheConstants.CACHE_POINT_CURR_VALUE, CommonService.toString(values));
		}
	}

	@Override
	public  String serializer(SensorData dto) throws NemsException {
		if (dto == null) return null;
		
		if (dto.getPid() == null)
			throw new NemsException("pid can't be null");
		if (dto.getDt() == null)
			throw new NemsException("dt can't be null");
		
		return dto.getPid()+","+dto.getV()+","+dto.getDt()+",0,";
	}

	@Override
	public  String serializer(PointValueDto dto) throws NemsException {
		if (dto == null) return null;
		
		if (dto.getPid() == null)
			throw new NemsException("pid can't be null");
		if (dto.getDt() == null)
			throw new NemsException("dt can't be null");
		if (dto.getQ() == null)
			dto.setQ(0);
		
		return dto.getPid()+","+dto.getV()+","+dto.getDt()+","+dto.getQ()
			+ (dto.getVt() == null ? "," : "," + dto.getVt());
	}

	@Override
	public PointValueDto deserializer(String value) throws Exception {
		if (StringUtils.isEmpty(value)) return null;
		
		String[] dataArray = value.split(",");
		if (dataArray.length < 4) {
			throw new NemsException("illegal cache point data format: " + value);
		}
		
		PointValueDto dto = new PointValueDto();
		
		setDtoPid(dto, dataArray[0]);
		if (pointValueFormat == 1) {
			setDtoV(dto, dataArray[1], null);
		} else {
			dto.setV(dataArray[1]);
		}
		setDtoDt(dto, dataArray[2]);
		setDtoQ(dto, dataArray[3]);
		if (dataArray.length > 4) {
			setDtoVt(dto, dataArray[4]);
		}
		
		return dto;
	}

	private Long setDtoPid(PointValueDto dto, String pidStr) {
		Long pid = null;
		try {
			pid = Long.parseLong(pidStr);
		} catch (NumberFormatException e) {
			pid = null;
		}
		dto.setPid(pid);
		return pid;
	}

	private Double setDtoV(PointValueDto dto, String v, Integer precision) throws Exception {
		if (StringUtils.isEmpty(v)) {
			return null;
		}
		Double d = null;
		try {
			d = Double.parseDouble(v);
			if (d.isInfinite()) {
				d = null;
			} else if (d.isNaN()) {
				d = null;
			} else if (precision != null && precision >= 0) {
				try {
					d = new BigDecimal(d).setScale(precision, BigDecimal.ROUND_HALF_UP).doubleValue();
				} catch (Exception e) {
					logger.error("set precision {} for {} failed!", precision, d);
				}
			}
		} catch (NumberFormatException ex) {
			d = null;
		} catch (Exception e) {
			logger.error("convert point value to Double failed!", e);
			throw e;
		}
		dto.setV(v);
		dto.setDv(d);
		if (d != null) {
			dto.setIv(d.intValue());
		}
		return d;
	}

	private Long setDtoDt(PointValueDto dto, String dtStr) {
		Long dt = null;
		try {
			dt = Long.parseLong(dtStr);
		} catch (NumberFormatException e) {
			dt = null;
		}
		dto.setDt(dt);
		return dt;
	}

	private Integer setDtoQ(PointValueDto dto, String qStr) {
		Integer q = null;
		try {
			q = Integer.parseInt(qStr);
		} catch (NumberFormatException e) {
			q = null;
		}
		dto.setQ(q);
		return q;
	}

	private Short setDtoVt(PointValueDto dto, String vtStr) {
		Short vt = null;
		try {
			vt = Short.parseShort(vtStr);
		} catch (NumberFormatException e) {
			vt = null;
		}
		dto.setVt(vt);
		return vt;
	}

	@Override
	public List<PointValueDto> parseMessage(List<ConsumerRecord<Integer, String>> msgs) {
		List<PointValueDto> datas = new ArrayList<PointValueDto>(msgs.size());
		for (ConsumerRecord<Integer, String> msg : msgs) {
			try {
				PointValueDto data = deserializer(msg.value());
				datas.add(data);
			} catch (NemsException e) {
				logger.error(e.getMessage());
				exceptionService.log(this.getClass().getName() + "-parseMessage", msg.value(), e);
			} catch (Exception e) {
				logger.error("parse msg {} failed!", msg.value(), e);
				exceptionService.log(this.getClass().getName() + "-parseMessage", msg.value(), e);
			}
		}
		return datas;
	}

	@Override
	public List<PointValueDto> getPointValuesList(Integer channelId, PointInfoDto point, Calendar cycleBegin) {
		//TODO 这里为什么不用zset呢？
		Map<String, String> hash = redisService.hgetall(Data.POINT_VALUES, channelId, 
				getPointValueCacheKey(point, cycleBegin));
		List<PointValueDto> values = new ArrayList<PointValueDto>(
				hash == null ? 0 : CommonService.getListInitCapacity(hash.size()));
		if (CommonService.isNotEmpty(hash)) {
			for (String v : hash.values()) {
				try {
					PointValueDto value = deserializer(v);
					values.add(value);
				} catch (Exception e) {
					logger.error("deserializer failed! {}", v, e);
				}
			}
		}
		values.sort((a, b)->{
			return a.getDt().compareTo(b.getDt());
		});
		return values;
	}

	@Override
	public List<PointValueDto> updatePointValueList(PointValueDto data, Calendar dateBegin) {
		List<PointValueDto> values = getPointValuesList(data.getPoint().getCalcChannel(), 
				data.getPoint(), dateBegin);
		//第一次创建key要设置过期时间
		boolean setExpire = values.isEmpty();
		
		List<PointValueDto> changedValues = new ArrayList<PointValueDto>();
		boolean exists = false;
		for (PointValueDto v : values) {
			if (v.getDt().equals(data.getDt())) {
				exists = true;
				v.setV(data.getV());
				v.setQ(data.getQ());
				changedValues.add(data);
				if (debug) {
					logger.debug("update point value list {}", CommonService.toString(data));
				}
				break;
			}
		}
		if (!exists) {
			values.add(data);
			changedValues.add(data);
			if (debug) {
				logger.debug("add to point value list {}", CommonService.toString(data));
			}
		}
		
		//保存覆盖有变化的部分到redis
		updatePointValuesList(data.getPoint().getCalcChannel(), data.getPoint(), 
				dateBegin, changedValues, setExpire);
		
		return values;
	}

	@Override
	public void updatePointValuesList(Integer channelId, PointInfoDto point, Calendar cycleBegin, 
			List<PointValueDto> values, Boolean setExpire) {
		Map<String, String> hash = new HashMap<String, String>();
		for (PointValueDto value : values) {
			try {
				hash.put(value.getDt().toString(), serializer(value));
			} catch (NemsException e) {
				logger.error("serializer failed! {}", value, e);
			}
		}
		String key = getPointValueCacheKey(point, cycleBegin);
		
		//TODO 这里为什么不用zset呢？
		redisService.hmset(Data.POINT_VALUES, channelId, key, hash);
		if (debug) {
			logger.debug("hmset {} : {} : {}", channelId, key, CommonService.toString(hash));
		}
		
		if (setExpire) {
			//第一次创建时要指定生命周期
			Long timeout = getPointValueCacheTimeout(point);
			if (timeout != null) {
				Boolean result = redisService.expire(Data.POINT_VALUES, channelId, key, timeout, TimeUnit.DAYS);
				if (debug) {
					logger.debug("expire {} : {} : {} days, result: {}", channelId, key, timeout, result);
				}
			}
		}
	}

	private String getPointValueCacheKey(PointInfoDto point, Calendar cycleBegin) {
		String key = CacheConstants.CACHE_POINT_VALUES+":"+point.getPointId();
		if (PointConstants.POINT_DATA_PERIOD_YEAR.equals(point.getDataPeriod())) {
			
		} else if (PointConstants.POINT_DATA_PERIOD_MONTH.equals(point.getDataPeriod())) {
			key += ":" + cycleBegin.get(Calendar.YEAR);
		} else if (PointConstants.POINT_DATA_PERIOD_DAY.equals(point.getDataPeriod())) {
			key += ":" + (cycleBegin.get(Calendar.YEAR) * 100 + cycleBegin.get(Calendar.MONTH) + 1);
		} else {
			key += ":" + (cycleBegin.get(Calendar.YEAR) * 10000 + cycleBegin.get(Calendar.MONTH) * 100 + 100 + 
					cycleBegin.get(Calendar.DAY_OF_MONTH));
		}
		return key;
	}

	private Long getPointValueCacheTimeout(PointInfoDto point) {
		if (PointConstants.POINT_DATA_PERIOD_YEAR.equals(point.getDataPeriod())) {
			return null;
		} else if (PointConstants.POINT_DATA_PERIOD_MONTH.equals(point.getDataPeriod())) {
			return 730L;
		} else if (PointConstants.POINT_DATA_PERIOD_DAY.equals(point.getDataPeriod())) {
			return 61L;
		} else {
			return 2L;
		}
	}

	@Override
	public boolean isValid(Integer q) {
		if (q == null) return false;
		if (q % 1000 < 100) return true;
		return false;
	}

	@Override
	public boolean isValid(PointValueDto v) {
		if (v == null) return false;
		if (v.getQ() == null || v.getV() == null) return false;
		if (v.getDv() == null || v.getDv().isInfinite() || v.getDv().isNaN()) return false;
		if (v.getQ() % 1000 < 100) return true;
		return false;
	}

	@Override
	public void setQByTime(PointValueDto v, String dataPeriod) {
		if (v == null || StringUtils.isEmpty(dataPeriod)) return;
		
		setQByTime(v, dataPeriod, new Date());
	}

	@Override
	public void setQByTime(PointValueDto v, String dataPeriod, Date now) {
		if (v == null || StringUtils.isEmpty(dataPeriod)) return;
		
		if (v.getDt() == null) {
			v.setQ(PointConstants.POINT_VALUE_OUTTIME);
		} else if (PointConstants.POINT_DATA_PERIOD_YEAR.equals(dataPeriod)) {
			Date begin = CommonService.trunc(now, TimeType.YEAR);
			if (v.getDt().longValue() < begin.getTime()) {
				v.setQ(PointConstants.POINT_VALUE_OUTTIME);
			}
		} else if (PointConstants.POINT_DATA_PERIOD_MONTH.equals(dataPeriod)) {
			Date begin = CommonService.trunc(now, TimeType.MONTH);
			if (v.getDt().longValue() < begin.getTime()) {
				v.setQ(PointConstants.POINT_VALUE_OUTTIME);
			}
		} else if (PointConstants.POINT_DATA_PERIOD_DAY.equals(dataPeriod)) {
			Date begin = CommonService.trunc(now, TimeType.DAY);
			if (v.getDt().longValue() < begin.getTime()) {
				v.setQ(PointConstants.POINT_VALUE_OUTTIME);
			}
		} else {
			//小时和实时数据都按3小时判断
			Date begin = CommonService.trunc(now, TimeType.HOUR);
			if (v.getDt().longValue() < begin.getTime() - 10800000L) {
				v.setQ(PointConstants.POINT_VALUE_OUTTIME);
			}
		}
	}

}
