package com.cie.nems.topology.distribute.sgcc;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.Constants;
import com.cie.nems.common.kafka.KafkaService;
import com.cie.nems.point.PointInfoDto;
import com.cie.nems.point.PointInfoService;
import com.cie.nems.topology.cache.point.PointCacheService;
import com.cie.nems.topology.cache.point.value.PointValueCacheService;
import com.cie.nems.topology.distribute.ly.LastValueDto;
import com.cie.nems.topology.distribute.ly.SensorData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SgccDistributeServiceImpl implements SgccDistributeService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.distr:#{false}}")
	private boolean debug;

	@Value("${cie.distr.data-filter-interval-ms:#{180000}}")
	private long dataFilterInterval;
	
	@Value("${cie.distr.data-accept-begin:#{0}}")
	private int dataAcceptBegin;
	
	@Value("${cie.distr.data-accept-end:#{0}}")
	private int dataAcceptEnd;

	@Autowired
	private PointCacheService pointCacheService;

	@Autowired
	private PointValueCacheService pointValueCacheService;

	@Autowired
	private PointInfoService pointInfoService;
	
	@Autowired
	private KafkaService kafkaService;

	private ObjectMapper om = new ObjectMapper();

	@Override
	public void execute(ConsumerRecord<Integer, String> msg) throws Exception {
		if (msg == null || msg.value() == null) return;
		String cmd = getCmd(msg.value());
		if (cmd != null) {
			if (ProtocolCmd.DATTA_SAME.toString().equals(cmd)) {
				/** 主动上送测点值（不同时标）: 客户端主动上送测点实时数据信息，每条测点值带自己的时标 */
				parsePointData(msg.value());
			} else if (ProtocolCmd.DATTA_DIFF.toString().equals(cmd)) {
				/** 主动上送测点值（同一时标）: 客户端主动上送测点实时数据信息，所有测点值的时标相同 */
				parsePointData(msg.value());
			} else if (ProtocolCmd.REGIST.toString().equals(cmd)) {
				/** 注册厂站: 客户端登陆成功后执行注册厂站，告诉Server端本连接可以发送多少个厂站的数据 */
				registStations(msg.value());
			} else if (ProtocolCmd.OFFLINE.toString().equals(cmd)) {
				/** 网关下线 */
				offline(msg.value());
			} else if (ProtocolCmd.LOGIN.toString().equals(cmd)) {
				/** 登陆: 所有交易在登录成功后才可以正常执行，否则Server端会立即中断该连接 */
				//TODO
			} else if (ProtocolCmd.UPLOAD_POINT.toString().equals(cmd)) {
				/** 上送厂站测点配置信息: Server下发招厂站配置信息命令客户端收到后将厂站测点配置信息发送至Server */
				//TODO
			} else if (ProtocolCmd.COLLECTER_ALARM.toString().equals(cmd)) {
				/** 采集器告警 */
				//TODO
			} else if (ProtocolCmd.QUERY.toString().equals(cmd)) {
				/** 召测测点值: Server端向客户端发送测点信息，主动获取需要的测点值信息 */
				//TODO
			} else if (ProtocolCmd.UPDATE.toString().equals(cmd)) {
				/** 更改厂站测点配置信息: 客户端将测点配置信息的更改及时通知到Server端 */
				//TODO
			} else if (ProtocolCmd.HEART.toString().equals(cmd)) {
				/** 链路测试（心跳）:  客户端周期性（每空闲15~30分钟）向Server端发送心跳报文，Server端会即时响应 */
				//TODO
			} else {
				logger.error("unknow cmd: {}", cmd);
			}
		}
	}

	private String getCmd(String msg) {
		//String msg = "{\"ip\":\"127.0.0.1\", \"timestamp\":152524313400, \"cmd\":\"FF\", \"data\":{...}}";
		if (msg == null) {
			logger.error("illegal msg: msg is null! {}", msg);
			return null;
		}
		int start = msg.indexOf("\"cmd\"");
		if (start == -1) {
			logger.error("illegal msg: no cmd field! {}", msg);
			return null;
		}
		if (msg.length() < start + 5) {
			logger.error("illegal msg: no cmd value! {}", msg);
			return null;
		}
		start = msg.indexOf("\"", start + 5);
		if (start == -1) {
			logger.error("illegal msg: no cmd value! {}", msg);
			return null;
		}
		String cmd = msg.substring(start + 1, start + 3);
		if ("0x".equals(cmd)) {
			cmd = msg.substring(start + 3, start + 5);
		}
		return cmd;
	}

	private void parsePointData(String msg) throws Exception {
		MsgDto dto = om.readValue(msg, new TypeReference<MsgDto>() {});
		if (dto == null) {
			return;
		}
		if (dto.getData() == null) {
			return;
		}
		if (dto.getData().getPointDataArray() == null || dto.getData().getPointDataArray().isEmpty()) {
			return;
		}
		if (ProtocolCmd.DATTA_SAME.toString().equals(dto.getCmd())) {
			sendSameTimeData(msg, dto);
		} else if (ProtocolCmd.DATTA_DIFF.toString().equals(dto.getCmd())) {
			sendDiffTimeData(msg, dto);
		}
	}

	private void sendSameTimeData(String msg, MsgDto dto) throws Exception {
		if (dto.getData().getTimeStamp() == null || dto.getData().getTimeStamp() == 0L) {
			return;
		}
		
		/* 为了节约服务器磁盘空间，设置接收数据的总开关时段 晚10点到凌晨3点的数据全部丢弃 */
		Long dt = dto.getData().getTimeStamp();
		LocalTime time = Instant.ofEpochMilli(dt)
				.atZone(ZoneOffset.ofHours(Constants.DEFAULT_ZONE_OFFSET_HOURS)).toLocalTime();
		int hhmm = time.getHour() * 100 + time.getMinute();
		if (dataAcceptBegin >= 0 && hhmm < dataAcceptBegin) {
			return;
		}
		if (dataAcceptEnd > 0 && hhmm >= dataAcceptEnd) {
			return;
		}
		LastValueDto lastValue = null;
		String preTopic = null;
		
		for (PointDataDto pd : dto.getData().getPointDataArray()) {
			if (pd.getPointId() == null || pd.getPointId() == 0L) {
				continue;
			}
			if (pd.getDataArray() == null || pd.getDataArray().isEmpty()) {
				continue;
			}
			for (DataDto d : pd.getDataArray()) {
				if (StringUtils.isEmpty(d.getValueType())) {
					continue;
				}
				
				//查找测点资料
				//TODO 未配置资料的先放进来送到一个默认通道，让数据先入库，避免丢数据
				PointInfoDto point = pointCacheService.getPointByPointId(pd.getPointId());
				if (point == null) {
					logger.info("pid {} not exists", pd.getPointId());
					continue;
				}
				
				/* 为防止采集器故障或恶意数据，过滤高频数据，保障后续程序处理压力不会太大 */
				if (pointInfoService.isYxPoint(point)) {
					//遥信存在突发告警的情况，所以要结合时间和值是否变化一起判断
					lastValue = pointValueCacheService.getLastValueByPid(pd.getPointId());
					if (lastValue != null) {
						if (StringUtils.equals(d.getValue(), lastValue.getV())) {
							if (lastValue.getDt() != null 
									&& Math.abs(dt - lastValue.getDt()) < dataFilterInterval) {
								continue;
							}
						}
					}
				} else {
					//遥测严格按时间过滤
					lastValue = pointValueCacheService.getLastValueByPid(pd.getPointId());
					if (lastValue != null && lastValue.getDt() != null 
							&& Math.abs(dt - lastValue.getDt()) < dataFilterInterval) {
						continue;
					}
				}
				pointValueCacheService.updateLastValue(pd.getPointId(), dt, d.getValue());
				
				//有datas来自一个数据包，必定是一个采集器上传，所以必定是一个电站的测点
				if (point.getCalcChannel() == null) {
					logger.info("pid {} has no calcChannel", pd.getPointId());
					continue;
				}
				
				if (preTopic == null) {
					preTopic = kafkaService.getPreTopicName(point.getCalcChannel());
				}
				
				//TODO dt小于当前时间24小时的，判断为历史数据，单独走历史数据topic
				
				//根据channel分发
				SensorData data = new SensorData();
				data.setPid(pd.getPointId());
				data.setV(d.getValue());
				data.setDt(dt);
				kafkaService.sendPoint(preTopic, data, debug);
			}
		}
	}

	private void sendDiffTimeData(String msg, MsgDto dto) throws Exception {
		Long dt = null;
		LocalTime time = null;
		int hhmm = 0;
		LastValueDto lastValue = null;
		String preTopic = null;
		
		for (PointDataDto pd : dto.getData().getPointDataArray()) {
			if (pd.getPointId() == null || pd.getPointId() == 0) {
				continue;
			}
			if (pd.getDataArray() == null || pd.getDataArray().isEmpty()) {
				continue;
			}
			if (pd.getTimeStamp() == null || pd.getTimeStamp() == 0L) {
				continue;
			}
			
			dt = pd.getTimeStamp();
			time = Instant.ofEpochMilli(dt)
					.atZone(ZoneOffset.ofHours(Constants.DEFAULT_ZONE_OFFSET_HOURS)).toLocalTime();
			hhmm = time.getHour() * 100 + time.getMinute();
			if (dataAcceptBegin >= 0 && hhmm < dataAcceptBegin) {
				continue;
			}
			if (dataAcceptEnd > 0 && hhmm >= dataAcceptEnd) {
				continue;
			}
			
			for (DataDto d : pd.getDataArray()) {
				if (StringUtils.isEmpty(d.getValueType())) {
					continue;
				}
				
				//查找测点资料
				//TODO 未配置资料的先放进来送到一个默认通道，让数据先入库，避免丢数据
				PointInfoDto point = pointCacheService.getPointByPointId(pd.getPointId());
				if (point == null) {
					logger.info("pid {} not exists", pd.getPointId());
					continue;
				}
				
				/* 为防止采集器故障或恶意数据，过滤高频数据，保障后续程序处理压力不会太大 */
				if (pointInfoService.isYxPoint(point)) {
					//遥信存在突发告警的情况，所以要结合时间和值是否变化一起判断
					lastValue = pointValueCacheService.getLastValueByPid(pd.getPointId());
					if (lastValue != null) {
						if (StringUtils.equals(d.getValue(), lastValue.getV())) {
							if (lastValue.getDt() != null 
									&& Math.abs(dt - lastValue.getDt()) < dataFilterInterval) {
								continue;
							}
						}
					}
				} else {
					//遥测严格按时间过滤
					lastValue = pointValueCacheService.getLastValueByPid(pd.getPointId());
					if (lastValue != null && lastValue.getDt() != null 
							&& Math.abs(dt - lastValue.getDt()) < dataFilterInterval) {
						continue;
					}
				}
				pointValueCacheService.updateLastValue(pd.getPointId(), dt, d.getValue());
				
				//有datas来自一个数据包，必定是一个采集器上传，所以必定是一个电站的测点
				if (point.getCalcChannel() == null) {
					logger.info("pid {} has no calcChannel", pd.getPointId());
					continue;
				}
				
				if (preTopic == null) {
					preTopic = kafkaService.getPreTopicName(point.getCalcChannel());
				}
				
				//TODO dt小于当前时间24小时的，判断为历史数据，单独走历史数据topic
				
				//根据channel分发
				SensorData data = new SensorData();
				data.setPid(pd.getPointId());
				data.setV(d.getValue());
				data.setDt(dt);
				kafkaService.sendPoint(preTopic, data, debug);
			}
		}
	}

	private void registStations(String msg) throws JsonMappingException, JsonProcessingException {
		OnlineOfflineDto dto = om.readValue(msg, new TypeReference<OnlineOfflineDto>() {});
		if (dto == null) return;
		if (dto.getData() == null) return;
		if (dto.getData().getStationId() == null || dto.getData().getStationId().isEmpty()) return;
		for (Integer stationId : dto.getData().getStationId()) {
			logger.debug("station {} regist", stationId);
		}
	}

	private void offline(String msg) throws JsonMappingException, JsonProcessingException {
		OnlineOfflineDto dto = om.readValue(msg, new TypeReference<OnlineOfflineDto>() {});
		if (dto == null) return;
		if (dto.getData() == null) return;
		if (dto.getData().getStationId() == null || dto.getData().getStationId().isEmpty()) return;
		for (Integer stationId : dto.getData().getStationId()) {
			logger.debug("station {} offline", stationId);
		}
	}

}
