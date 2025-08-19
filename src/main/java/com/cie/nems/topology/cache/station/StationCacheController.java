package com.cie.nems.topology.cache.station;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cie.nems.station.Station;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/station")
@Api(tags="电站缓存接口")
public class StationCacheController {

	@Autowired
	private StationCacheService stationCacheService;

	@ApiOperation(value="查询电站缓存长度", notes="返回电站缓存列表长度")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getStationCacheSize() {
		return ResponseEntity.ok(stationCacheService.getStationCacheSize());
	}

	@ApiOperation(value="根据stationId查询电站信息缓存", notes="返回一个map，key为stationId，value为电站信息对象")
	@RequestMapping(value = "stations", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Station>> getStationCache(@RequestParam(required = false) List<String> stationIds) {
		return ResponseEntity.ok(stationCacheService.getStationList(stationIds));
	}
}
