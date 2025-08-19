package com.cie.nems.monitor;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.cie.nems.common.service.CommonService;

@Component
public class MonitorCenterDao {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public void setMapValue(Map<String, Object> data, String key, 
			int type, ResultSet rs) throws SQLException {
		if (type == Types.VARCHAR) {
			data.put(key, rs.getString(key));
		} else if (type == Types.DATE) {
			Date time = rs.getDate(key);
			data.put(key, time == null ? null : time.getTime());
		} else if (type == Types.TIMESTAMP) {
			Date time = rs.getDate(key);
			data.put(key, time == null ? null : time.getTime());
		} else if (type == Types.INTEGER) {
			data.put(key, rs.getObject(key, Integer.class));
		} else if (type == Types.DOUBLE) {
			BigDecimal v = rs.getBigDecimal(key);
			data.put(key, v == null ? null : v.doubleValue());
		} else if (type == Types.BIGINT) {
			data.put(key, rs.getObject(key, Long.class));
		} else if (type == Types.DECIMAL) {
			BigDecimal v = rs.getBigDecimal(key);
			data.put(key, v == null ? null : v.doubleValue());
		} else if (type == Types.FLOAT) {
			BigDecimal v = rs.getBigDecimal(key);
			data.put(key, v == null ? null : v.floatValue());
		} else if (type == Types.NUMERIC) {
			BigDecimal v = rs.getBigDecimal(key);
			data.put(key, v == null ? null : v.doubleValue());
		} else if (type == Types.BOOLEAN) {
			data.put(key, rs.getObject(key, Boolean.class));
		}
	}

	public static final String LAST_SAVE_TIME = "_lastSaveTime";
	public SqlParameterSource[] getSqlParameterSourcesByMap(List<Map<String, Object>> datas, 
			Map<String, Integer> columnTypes) {
		SqlParameterSource[] params = new SqlParameterSource[datas.size()];
		for (int i=0; i<datas.size(); ++i) {
			MapSqlParameterSource mps = new MapSqlParameterSource();
			Map<String, Object> data = datas.get(i);
			Double dv = null;
			for (Entry<String, Object> e : data.entrySet()) {
				if (LAST_SAVE_TIME.equals(e.getKey())) continue;
				try {
					int type = columnTypes.get(e.getKey());
					if (type == Types.DATE || type == Types.TIMESTAMP) {
						mps.addValue(e.getKey(), e.getValue() == null ? 
								null : new Date((Long) e.getValue()), type);
					} else if (type == Types.DOUBLE) {
						dv = (Double) e.getValue();
						if (dv != null && dv > 999999999999.999999) {
							dv = null;
						}
						mps.addValue(e.getKey(), dv , type);
					} else {
						mps.addValue(e.getKey(), e.getValue(), type);
					}
				} catch(Exception ex) {
					logger.error("get sql param failed! {} : {}", CommonService.toString(e), ex.getMessage());
				}
			}
			params[i] = mps;
		}
		return params;
	}

}
