package com.cie.nems.topology.cache.device;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;

import com.cie.nems.device.Device;
import com.cie.nems.objRela.ObjRela;
import com.cie.nems.topology.cache.CountDto;

public interface DeviceCacheService {
	public static int deviceBatchNumber = 2000;

	public int updateDevices(List<Device> deviceList);

	public int deleteDevices(List<Device> deviceList);

	public int updateDeviceRelas(List<ObjRela> relas);

	public int deleteDeviceRelasByParentIds(List<String> parentIds);
	public int deleteDeviceRelasByChildIds(List<String> childIds);
	public int deleteDeviceRelasByPointIds(List<Long> pointIds);

	public Device getDeviceByPsrId(String psrId);

	public Device getDeviceByDeviceId(String deviceId);

	public Map<String, Device> getDevices();

	public List<Device> getDevicesByStationId(String stationId, Set<String> deviceTypes);

	public Map<String, Device> getDevicesByDeviceIds(List<String> deviceIds);

	public Map<String, Integer> getDeviceCacheSize();

	public Map<String, Integer> getStationCount();

	public Map<String, Integer> getStationDeviceTypeCount(String stationId);

	public Page<Device> initDevices(int page, List<Integer> channelIds, List<String> deviceIds, List<String> psrIds, 
			CountDto count);

	public ObjRela getPointRelaDevice(Long pointId);

	public Integer getDeviceChildrenCount();

	public List<ObjRela> getDeviceChildren(String deviceId);

	public Integer getDeviceParentsCount();

	public List<ObjRela> getDeviceParents(String deviceId);

}
