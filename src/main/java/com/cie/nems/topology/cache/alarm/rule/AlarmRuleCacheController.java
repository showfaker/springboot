package com.cie.nems.topology.cache.alarm.rule;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cie.nems.alarm.filter.AlarmFilter;
import com.cie.nems.alarm.rule.AlarmRule;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/alarm/rule")
@Api(tags="告警规则缓存接口")
public class AlarmRuleCacheController {

	@Autowired
	private AlarmRuleCacheService alarmRuleCacheService;

	@ApiOperation(value="查询告警规则缓存长度", notes="返回一个map，展示有告警规则的测点数、告警规则数、电站外线告警数")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getAlarmRuleCacheSize() {
		return ResponseEntity.ok(alarmRuleCacheService.getAlarmRuleCacheSize());
	}

	@ApiOperation(value="根据ruleId查询告警规则缓存信息", notes="返回一个map，key为ruleId，value为告警规则对象")
	@RequestMapping(value = "rules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<Long, AlarmRule>> getAlarmRuleCache(@RequestParam(required = true) List<Long> ruleIds) {
		return ResponseEntity.ok(alarmRuleCacheService.getAlarmRules(ruleIds));
	}

	@ApiOperation(value="根据pointId查询其关联的告警规则缓存", notes="返回用于该测点的告警规则对象列表")
	@RequestMapping(value = "pointRules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AlarmRule>> getObjAlarmRules(@RequestParam(required = true) Long pointId) {
		return ResponseEntity.ok(alarmRuleCacheService.getPointAlarmRules(pointId));
	}

	@ApiOperation(value="根据deviceId查询其告警过滤规则缓存", notes="返回适用于该设备的告警过滤规则列表")
	@RequestMapping(value = "deviceFilters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AlarmFilter>> getDeviceFilters(@RequestParam(required = true) String deviceId) {
		return ResponseEntity.ok(alarmRuleCacheService.getDeviceAlarmFilters(deviceId));
	}

	@ApiOperation(value="根据stationId查询其告警过滤规则缓存", notes="返回适用于该电站的告警过滤规则列表")
	@RequestMapping(value = "stationFilters", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<AlarmFilter>> getStationFilters(@RequestParam(required = true) String stationId) {
		return ResponseEntity.ok(alarmRuleCacheService.getStationAlarmFilters(stationId));
	}

	@ApiOperation(value="根据stationId查询其外线告警规则缓存", notes="返回一个map，key为stationId，value为适用于该电站的离线告警规则（alarm_source为02）")
	@RequestMapping(value = "stationOutlineRules", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<AlarmRule>>> getStationOutlineRules(@RequestParam(required = false) String stationId) {
		return ResponseEntity.ok(alarmRuleCacheService.getStationOutlineRules(stationId));
	}

}
