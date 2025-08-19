package com.cie.nems.topology;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.NemsException;
import com.cie.nems.common.service.CommonService;

@Service
public class CalcTopoServiceImpl implements CalcTopoService {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.id}")
	private String appId;
	
	@Autowired
	private CalcTopoCfgRepository mqRepo;

	@Autowired
	private CommonService commonService;
	
	private Map<String, CalcTopoCfg> calcTopoCfgs = null;

	@Override
	public void initCalcTopoCfgs() throws NemsException {
		String ip = null;
		try {
			ip = commonService.getLocalHostLANAddress().getHostAddress();
		} catch (Exception e) {
			logger.error("get local host address failed!", e);
		}
		
		logger.info("find cfg for ip: {}, app.id: {}", ip, appId);
		List<CalcTopoCfg> cfgs = mqRepo.findByTopoIpAndTopoApp(ip, appId);
		if (CommonService.isEmpty(cfgs)) {
			throw new NemsException("calc_topo_cfg 中找不到本实例的配置信息!");
		}
		
		int activeTopos = 0;
		StringBuffer info = new StringBuffer();
		info.append("\n****************************************************************");
		Map<String, CalcTopoCfg> cfgMap = new HashMap<String, CalcTopoCfg>();
		for (CalcTopoCfg cfg : cfgs) {
			if (cfg.getClientNum() == null || cfg.getClientNum() <= 0) continue;
			
			if (StringUtils.isBlank(cfg.getChannelIds())) {
				throw new NemsException("calc_topo_cfg[id: "+cfg.getChannelIds()+"] 中未指定计算通道!");
			}
			
			cfgMap.put(cfg.getClientPrefix(), cfg);
			info.append("\n* topic[").append(cfg.getTopoStatus()).append("]: ").append(cfg.getTopic())
				.append(", clientPrefix: ").append(cfg.getClientPrefix())
				.append(", clientNum: ").append(cfg.getClientNum())
				.append(", channels: ").append(cfg.getChannelIds());
			
			if (CalcTopoService.CALC_TOPO_STATUS_ACTIVE.equals(cfg.getTopoStatus())) {
				++activeTopos;
			}
		}
		info.append("\n****************************************************************");
		
		this.calcTopoCfgs = cfgMap;
		
		logger.info(info.toString());
		
		if (activeTopos == 0) {
			throw new NemsException("没有启用任何拓扑，请检查calc_topo_cfg表配置!");
		}
	}
	
	@Override
	public Map<String, CalcTopoCfg> getCalcTopoCfgs() throws NemsException {
		if (calcTopoCfgs == null) {
			initCalcTopoCfgs();
		}
		return calcTopoCfgs;
	}

	private List<Integer> channelIds = null;
	@Override
	public List<Integer> getChannelIds() throws NemsException {
		if (channelIds == null) {
			channelIds = getChannelIds(getCalcTopoCfgs());
		}
		if (CommonService.isEmpty(channelIds)) {
			throw new NemsException("未指定需要处理的通道号!");
		}
		return channelIds;
	}

	private List<Integer> getChannelIds(Map<String, CalcTopoCfg> cfgs) {
		Set<Integer> channelIds = new HashSet<Integer>();
		for (CalcTopoCfg cfg : cfgs.values()) {
			if (CalcTopoService.CALC_TOPO_STATUS_ACTIVE.equals(cfg.getTopoStatus())) {
				String[] ids = StringUtils.split(cfg.getChannelIds(), ",");
				for (String id : ids) {
					if (StringUtils.isNotBlank(id)) {
						channelIds.add(Integer.valueOf(id));
					}
				}
			}
		}
		return new ArrayList<Integer>(channelIds);
	}

	@Override
	public Integer getConcurrency(String clientId) throws NemsException {
		CalcTopoCfg cfg = calcTopoCfgs.get(clientId);
		if (cfg == null) {
			throw new NemsException("no config found for clientId: "+clientId);
		}
		if (cfg.getClientNum() == null) {
			throw new NemsException("config.client_num is null for clientId: "+clientId);
		}
		if (cfg.getClientNum() == 0) {
			throw new NemsException("config.client_num is 0 for clientId: "+clientId);
		}
		return cfg.getClientNum();
	}

	@Override
	public boolean isDistr() {
		boolean distrActive = false, preActive = false;
		for (CalcTopoCfg cfg : calcTopoCfgs.values()) {
			if (CalcTopoService.CALC_TOPO_STATUS_ACTIVE.equals(cfg.getTopoStatus())) {
				if (CalcTopoService.CLIENT_ID_DISTR.equals(cfg.getClientPrefix())) {
					distrActive = true;
				} else if (CalcTopoService.CLIENT_ID_PRE.equals(cfg.getClientPrefix())) {
					preActive = true;
				}
			}
		}
		//分发程序只启用一个配置
		return !preActive && distrActive;
	}

}
