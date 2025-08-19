package com.cie.nems.topology.cmd;

import com.cie.nems.common.service.CommonService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.exception.ExceptionService;
import com.cie.nems.topology.cmd.device.DeviceCmdService;
import com.cie.nems.topology.cmd.station.StationCmdService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class CmdServiceImpl implements CmdService {

	@Value("${cie.app.debug.cmd:#{false}}")
	private boolean debug;

	@Autowired
	private ExceptionService exceptionService;
	
	@Autowired
	private StationCmdService stationCmdService;
	
	@Autowired
	private DeviceCmdService deviceCmdService;

	private ObjectMapper om = new ObjectMapper();

	@Override
	public void execute(ConsumerRecord<Integer, String> msg) {
		String payload = new String(msg.value());

		JsonNode json;
		try {
			json = CommonService.om.readTree(payload);
		} catch(Exception e) {
			log.error("parse payload failed : {}", payload, e);
			return;
		}

		if (json == null || json.isNull() || json.isEmpty()) {
			log.error("payload is null or empty : {}", payload);
			return;
		}

		String action = CommonService.getString(json.get("action"));
		if (StringUtils.isEmpty(action)) {
			log.error("action is empty : {}", payload);
			return;
		}

		if ("updateDeviceCache".equals(action)) {
			// 新增设备
			Integer channel = CommonService.getInt(json.get("channel"));
			if (channel == null) {
				log.error("channel is empty : {}", payload);
				return;
			}
			JsonNode deviceIdsNode = json.get("deviceIds");
			if (deviceIdsNode == null || deviceIdsNode.isNull() || deviceIdsNode.isEmpty()) {
				log.error("deviceIds is empty : {}", payload);
				return;
			} else if (!deviceIdsNode.isArray()) {
				log.error("deviceIds is not array : {}", payload);
				return;
			}
			List<String> deviceIds = new ArrayList<>();
			for (JsonNode idNode : deviceIdsNode) {
				String deviceId = CommonService.getString(idNode);
				if (deviceId != null)
					deviceIds.add(deviceId);
			}
			if (deviceIds.isEmpty()) {
				log.error("deviceIds is empty : {}", payload);
				return;
			}

			try {
				// cacheService.updateDevicePointCache(deviceIds);
			} catch (Exception e) {
				log.error("create device failed!", e);
			}
		}
//		CmdDto cmd = parseMessage(msg);
//
//		if (cmd == null) return;
//
//		if (Cmd.updateStationInfo == cmd.getCmd()) {
//			stationCmdService.updateStationInfo(cmd);
//		} else if (Cmd.deleteStationInfo == cmd.getCmd()) {
//			stationCmdService.deleteStationInfo(cmd);
//		} else if (Cmd.updateDeviceInfo == cmd.getCmd()) {
//			deviceCmdService.updateDeviceInfo(cmd);
//		} else if (Cmd.deleteDeviceInfo == cmd.getCmd()) {
//			deviceCmdService.deleteDeviceInfo(cmd);
//		}
	}

	private CmdDto parseMessage(ConsumerRecord<Integer, String> msg) {
		try {
			CmdDto cmd = om.readValue(msg.value(), new TypeReference<CmdDto>() {});
			return cmd;
		} catch (Exception e) {
			log.error("parse msg failed! {} : {}", msg.value(), e.getMessage());
			exceptionService.log(this.getClass().getName() + "-parseMessage", "msg", e);
		}
		return null;
	}

}
