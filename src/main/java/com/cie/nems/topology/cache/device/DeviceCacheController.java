package com.cie.nems.topology.cache.device;

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

import com.cie.nems.device.Device;
import com.cie.nems.objRela.ObjRela;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/cache/device")
@Api(tags="设备信息缓存接口")
public class DeviceCacheController {

	@Autowired
	private DeviceCacheService deviceCacheService;

	@ApiOperation(value="查询设备信息缓存长度", notes="返回一个map，展示psrId缓存长度和deviceId缓存长度")
	@RequestMapping(value = "size", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getDeviceCacheSize() {
		return ResponseEntity.ok(deviceCacheService.getDeviceCacheSize());
	}

	@ApiOperation(value="按电站统计设备数", notes="返回一个map，key为stationId，value为该电站下设备缓存数")
	@RequestMapping(value = "stationCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getStationCount() {
		return ResponseEntity.ok(deviceCacheService.getStationCount());
	}

	@ApiOperation(value="按deviceType统计设备数", notes="返回一个map，key为deviceType，value为该类型的设备缓存数")
	@RequestMapping(value = "deviceTypeCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Integer>> getStationDeviceTypeCount(@RequestParam(required = true) String stationId) {
		return ResponseEntity.ok(deviceCacheService.getStationDeviceTypeCount(stationId));
	}

	@ApiOperation(value="根据deviceId查询设备信息缓存", notes="返回一个map，key为deviceId，value为设备对象")
	@RequestMapping(value = "devices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Map<String, Device>> getDeviceCache(@RequestParam(required = true) List<String> deviceIds) {
		return ResponseEntity.ok(deviceCacheService.getDevicesByDeviceIds(deviceIds));
	}

	@ApiOperation(value="根据stationId查询设备缓存列表", notes="返回指定电站在设备信息缓存中所有的设备对象列表")
	@RequestMapping(value = "stationDevices", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<Device>> getStationDeviceCache(@RequestParam(required = true) String stationId,
			@RequestParam(required = false) Set<String> deviceTypes) {
		return ResponseEntity.ok(deviceCacheService.getDevicesByStationId(stationId, deviceTypes));
	}

	@ApiOperation(value="有子设备的设备数", notes="返回有子设备的设备数")
	@RequestMapping(value = "deviceChildrenCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getDeviceChildrenCount() {
		return ResponseEntity.ok(deviceCacheService.getDeviceChildrenCount());
	}

	@ApiOperation(value="查询指定设备的子设备", notes="查询指定设备的子设备")
	@RequestMapping(value = "deviceChildren", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ObjRela>> getDeviceChildren(@RequestParam(required = true) String deviceId) {
		return ResponseEntity.ok(deviceCacheService.getDeviceChildren(deviceId));
	}

	@ApiOperation(value="有父设备的设备数", notes="返回有父设备的设备数")
	@RequestMapping(value = "deviceParentsCount", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Integer> getDeviceParentsCount() {
		return ResponseEntity.ok(deviceCacheService.getDeviceParentsCount());
	}

	@ApiOperation(value="查询指定设备的父设备", notes="查询指定设备的父设备")
	@RequestMapping(value = "deviceParents", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<List<ObjRela>> getDeviceParents(@RequestParam(required = true) String deviceId) {
		return ResponseEntity.ok(deviceCacheService.getDeviceParents(deviceId));
	}

	@ApiOperation(value="根据测点查询对应设备", notes="根据测点查询对应设备")
	@RequestMapping(value = "pointRelaDevice", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ObjRela> getPointRelaDevice(@RequestParam(required = true) Long pid) {
		return ResponseEntity.ok(deviceCacheService.getPointRelaDevice(pid));
	}

}
