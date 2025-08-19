package com.cie.nems.topology.cache.monitor.device;

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
@RequestMapping("/api/cache/monitor/device")
@Api(tags="设备中间表缓存接口")
public class DeviceMonitorCacheController {

	@Autowired
	private DeviceMonitorCacheService deviceMonitorCacheService;

	@ApiOperation(value="查询deviceMonitorReal缓存长度", notes="")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getDeviceCacheSize() {
		return ResponseEntity.ok(deviceMonitorCacheService.getDeviceMonitorCacheSize());
	}

	@ApiOperation(value="查询deviceMonitorReal缓存中的deviceId集合", notes="将缓存内所有deviceId作为一个set返回")
	@RequestMapping(value = "deviceIds", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Set<String>> getStationIds() {
		return ResponseEntity.ok(deviceMonitorCacheService.getDeviceIds());
	}

	@ApiOperation(value="根据deviceId查询deviceMonitorReal缓存信息", notes="返回一个列表，列表内每个对象是一个设备的deviceMonitorReal缓存")
	@RequestMapping(value = "datas", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Map<String, Object>>> getDeviceMonitorCache(@RequestParam(required = true) List<String> deviceIds) {
		return ResponseEntity.ok(deviceMonitorCacheService.getDeviceMonitorReals(deviceIds));
	}

}
