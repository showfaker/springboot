package com.cie.nems.topology.cache.alarm.log;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cie.nems.alarm.log.AlarmLogs;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/alarm/log")
@Api(tags="告警记录缓存接口")
public class AlarmLogCacheController {

	@Autowired
	private AlarmLogCacheService alarmLogCacheService;

	@ApiOperation(value="查询告警记录缓存长度", notes="返回发生过告警的测点数")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getAlarmRuleCacheSize() {
		return ResponseEntity.ok(alarmLogCacheService.getAlarmStatusCacheSize());
	}

	@ApiOperation(value="根据测点ID查询其告警状态", notes="返回一个map，key为告警规则ruleId，value为该规则最后一条告警记录缓存")
	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<Long, AlarmLogs>> getAlarmRuleCache(@RequestParam(required = true) Long pointId) {
		return ResponseEntity.ok(alarmLogCacheService.getPointAlarmStatus(pointId));
	}

}
