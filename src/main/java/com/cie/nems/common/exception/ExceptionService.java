package com.cie.nems.common.exception;

import java.util.Map;

import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;

public interface ExceptionService {

	public Map<String, ExceptionInfoDto> getExceptions();

	public void log(String key, PointValueDto data, Exception e);

	public void log(String key, SensorData data, Exception e);

	public void log(String key, String data, Exception e);

	public void log(String key, String data);

}
