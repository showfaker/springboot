package com.cie.nems.topology;

import java.util.List;
import java.util.Map;

import com.cie.nems.common.exception.NemsException;

public interface CalcTopoService {
	
	public static final String CLIENT_ID_DISTR = "distr";
	public static final String CLIENT_ID_PRE = "pre";
	public static final String CLIENT_ID_ALARM = "alarm";
	public static final String CLIENT_ID_SAVE = "save";
	public static final String CLIENT_ID_EXPRESSION_CALC = "expression-calc";
	public static final String CLIENT_ID_DEVICE_CALC = "device-calc";
	public static final String CLIENT_ID_STATION_CALC = "station-calc";
	public static final String CLIENT_ID_MONITOR_CENTER = "monitor-center";
	public static final String CLIENT_ID_CMD = "cmd";

	public static final String CALC_TOPO_STATUS_ACTIVE = "1";
	public static final String CALC_TOPO_STATUS_INACTIVE = "0";
	
	public void initCalcTopoCfgs() throws NemsException;

	/**
	 * Map(clientPrefix, cfg)</br>
	 * {</br>
	 * &nbsp;&nbsp;distr: ...</br>
	 * &nbsp;&nbsp;calc: ...</br>
	 * &nbsp;&nbsp;save: ...</br>
	 * &nbsp;&nbsp;alarm: ...</br>
	 * &nbsp;&nbsp;...</br>
	 * }
	 */
	public Map<String, CalcTopoCfg> getCalcTopoCfgs() throws NemsException;

	public Integer getConcurrency(String topic) throws NemsException;

	public List<Integer> getChannelIds() throws NemsException;

	public boolean isDistr();

}
