package com.cie.nems.common.parameter;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParamServiceImpl implements ParamService {
	@Autowired
	private AppParameterRepository paramRepo;

	@Override
	public AppParameter getAppParameter(String paramCode) {
		if (StringUtils.isEmpty(paramCode)) return null;
		
		Optional<AppParameter> opt = paramRepo.findById(paramCode);
		if (opt.isPresent()) {
			return opt.get();
		} else {
			return null;
		}
	}

}
