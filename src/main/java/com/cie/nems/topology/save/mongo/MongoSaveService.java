package com.cie.nems.topology.save.mongo;

import java.util.List;

import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface MongoSaveService {

	public static final String NEMS_VALUES_PREFIX = "nems_values_";
	public static final String NEMS_VALUES_HOUR_PREFIX = "nems_values_hour_";
	public static final String NEMS_VALUES_DAY = "nems_values_day";
	public static final String NEMS_VALUES_MON = "nems_values_month";
	public static final String NEMS_VALUES_YEAR = "nems_values_year";

	public void save(List<PointValueDto> datas);

}
