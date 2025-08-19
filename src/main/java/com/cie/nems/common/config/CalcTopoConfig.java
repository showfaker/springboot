package com.cie.nems.common.config;

import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import com.cie.nems.common.parameter.AppParameter;
import com.cie.nems.common.parameter.ParamService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.common.util.SpringContextUtil;
import com.cie.nems.topology.CalcTopoCfg;
import com.cie.nems.topology.CalcTopoService;

@Configuration
public class CalcTopoConfig implements InitializingBean {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ParamService paramService;

	@Autowired
	private CalcTopoService calcTopoService;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			AppParameter param = paramService.getAppParameter(ParamService.PARAM_CODE_TOPOLOGY);
			
			StringBuffer buf = new StringBuffer();
			buf.append("\n****************************************************************");
			
			Map<String, String> param1 = CommonService.readValue(param.getParam1());
			
			for (Entry<String, String> e : param1.entrySet()) {
				if (StringUtils.isBlank(e.getValue())) {
					buf.append("\n* parameter ").append(e.getKey()).append(" is blank");
				} else {
					System.setProperty(e.getKey(), e.getValue());
					buf.append("\n* setProperty(").append(e.getKey()).append(", ").append(e.getValue()).append(")");
				}
			}
			
			Map<String, Integer> param2 = CommonService.readValue(param.getParam2());
			
			for (Entry<String, Integer> e : param2.entrySet()) {
				if (e.getValue() == null) {
					buf.append("\n* parameter ").append(e.getKey()).append(" is null");
				} else {
					System.setProperty(e.getKey(), String.valueOf(e.getValue()));
					buf.append("\n* setProperty(").append(e.getKey()).append(", ").append(e.getValue()).append(")");
				}
			}
			
			buf.append("\n****************************************************************");
			
			Map<String, CalcTopoCfg> cfgs = calcTopoService.getCalcTopoCfgs();
			
			for (Entry<String, CalcTopoCfg> e : cfgs.entrySet()) {
				String key = e.getValue().getClientPrefix() + "-topic-name";
				System.setProperty(key, e.getValue().getTopic());
				buf.append("\n* setProperty(").append(key).append(", ").append(e.getValue().getTopic()).append(")");
			}
			
			buf.append("\n****************************************************************");
			logger.info(buf.toString());
		} catch (Exception e) {
			logger.error("init topology parameters failed, System terminated!", e);
			//throw e;
			System.exit(SpringContextUtil.EXIT_FAIL);
		}
	}

}
