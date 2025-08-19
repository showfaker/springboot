package com.cie.nems.topology.cache.suntime;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cie.nems.suntime.SunTime;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/suntime")
@Api(tags="日出日落时间配置缓存接口")
public class SunTimeCacheController {

	@Autowired
	private SunTimeCacheService sunTimeCacheService;

	@ApiOperation(value="查询日出日落配置缓存长度", notes="")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getAlarmRuleCacheSize() {
		return ResponseEntity.ok(sunTimeCacheService.getSunTimeCacheSize());
	}

	@ApiOperation(value="查询全量日出日落时间缓存信息", notes="返回一个map，key为地区regionId，value为该地区的日出日落时间配置列表")
	@RequestMapping(value = "suntimes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, List<SunTime>>> getAlarmRuleCache() {
		return ResponseEntity.ok(sunTimeCacheService.getSunTimes());
	}

	@ApiOperation(value="根据地区regionId查询日出日落时间缓存信息", notes="返回该地区的日出日落时间配置列表")
	@RequestMapping(value = "regionSuntimes", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<SunTime>> getObjAlarmRules(@RequestParam(required = true) String regionId) {
		return ResponseEntity.ok(sunTimeCacheService.getSunTimes(regionId));
	}

}
