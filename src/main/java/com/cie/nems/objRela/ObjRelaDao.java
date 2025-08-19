package com.cie.nems.objRela;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cie.nems.common.jdbc.MyJdbcTemplate;

@Component
public class ObjRelaDao {

	@Autowired
	private MyJdbcTemplate myJdbcTemp;
	
	public List<ObjRela> getDeviceRelas(List<String> parentIds) {
		String sql = "select r.obj_id1, r.obj_id2, r.point_id, r.param1, d1.psr_id, d2.psr_id "
				+ "from obj_rela r "
				+ "join device d1 on r.obj_id1 = d1.device_id "
				+ "join device d2 on r.obj_id2 = d2.device_id "
				+ "where r.rela_type = '01' and  r.obj_id1 in (:parentIds) ";
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("parentIds", parentIds);
		return myJdbcTemp.queryForBeanList(sql, paramMap, ObjRela.class);
	}

}
