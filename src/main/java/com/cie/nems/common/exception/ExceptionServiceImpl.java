package com.cie.nems.common.exception;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;

@Service
public class ExceptionServiceImpl implements ExceptionService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PointValueCacheService pointValueCacheService;

	private Map<String, ExceptionInfoDto> exceptions = new ConcurrentHashMap<String, ExceptionInfoDto>();

	@Override
	public Map<String, ExceptionInfoDto> getExceptions() {
		return exceptions;
	}

	@Override
	public void log(String key, PointValueDto data, Exception e) {
		try {
			String str = data == null ? null : pointValueCacheService.serializer(data);
			log(key, str, e);
		} catch (Exception e1) {
			logger.error("save exception info failed!", e);
		}
	}

	@Override
	public void log(String key, SensorData data, Exception e) {
		try {
			String str = data == null ? null : pointValueCacheService.serializer(data);
			log(key, str, e);
		} catch (Exception e1) {
			logger.error("save exception info failed!", e);
		}
	}

	@Override
	public void log(String key, String data, Exception e) {
		if (e != null) {
			key = key + "-" + e.getMessage();
		}
		ExceptionInfoDto info = exceptions.get(key);
		if (info == null) {
			info = new ExceptionInfoDto();
			exceptions.put(key, info);
		}
		info.setData(data);
		info.setCount(info.getCount() + 1L);
		info.setLastTime(System.currentTimeMillis());
		if (e != null) {
			info.setErrorMessage(e.getMessage());
		}
	}

	@Override
	public void log(String key, String data) {
		log(key, data, null);
	}

}
