package com.cie.nems.topology.cache.monitor.station;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/monitor/station")
@Api(tags="电站中间表缓存接口")
public class StationMonitorCacheController {

	@Autowired
	private StationMonitorCacheService stationMonitorCacheService;

	@ApiOperation(value="查询stationMonitorReal缓存长度", notes="")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getStationCacheSize() {
		return ResponseEntity.ok(stationMonitorCacheService.getStationMonitorCacheSize());
	}

	@ApiOperation(value="查询stationMonitorReal缓存中的stationId集合", notes="将缓存内所有stationId作为一个set返回")
	@RequestMapping(value = "stationIds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<String>> getStationIds() {
		return ResponseEntity.ok(stationMonitorCacheService.getStationIds());
	}

	@ApiOperation(value="根据stationId查询stationMonitorReal缓存信息", notes="返回一个列表，列表内每个对象是一个电站的stationMonitorReal缓存")
	@RequestMapping(value = "datas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, Object>>> getStationMonitorCache(@RequestParam(required = true) List<String> stationIds) {
		return ResponseEntity.ok(stationMonitorCacheService.getStationMonitorReals(stationIds));
	}

}
