package com.cie.nems.topology.save.postgre;

import java.util.List;

import com.cie.nems.topology.cache.point.value.PointValueDto;

public interface PostgreSaveService {

	public void save(List<PointValueDto> datas);

}
