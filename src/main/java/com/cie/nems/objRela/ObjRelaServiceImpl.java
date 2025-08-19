package com.cie.nems.objRela;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;

@Service
public class ObjRelaServiceImpl implements ObjRelaService {

	@Autowired
	private ObjRelaDao objRelaDao;
	
	@Override
	public List<ObjRela> getDeviceRelas(List<String> parentIds) {
		if (CommonService.isEmpty(parentIds)) return null;
		
		return objRelaDao.getDeviceRelas(parentIds);
	}

}
