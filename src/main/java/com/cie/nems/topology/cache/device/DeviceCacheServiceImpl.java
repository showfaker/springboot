package com.cie.nems.topology.cache.device;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;
import com.cie.nems.device.Device;
import com.cie.nems.device.DeviceService;
import com.cie.nems.objRela.ObjRela;
import com.cie.nems.objRela.ObjRelaService;
import com.cie.nems.station.Station;
import com.cie.nems.topology.alarm.offline.OfflineAlarmService;
import com.cie.nems.topology.cache.CountDto;
import com.cie.nems.topology.cache.station.StationCacheService;

@Service
public class DeviceCacheServiceImpl implements DeviceCacheService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private StationCacheService stationCacheService;

	@Autowired
	private DeviceService deviceService;

	@Autowired
	private ObjRelaService objRelaService;

	/**
	 * Map(psrId, Device)
	 */
	private Map<String, Device> psrDevices = new ConcurrentHashMap<String, Device>();
	
	/**
	 * Map(deviceId, Device)
	 */
	private Map<String, Device> devices = new ConcurrentHashMap<String, Device>();
	
	/**
	 * Map(parentDeviceId, childRelas)</br>
	 * 设备ID与其子设备关系
	 */
	private Map<String, List<ObjRela>> deviceChildrenMap = new ConcurrentHashMap<String, List<ObjRela>>();
	/**
	 * Map(childDeviceId, parentRelas)</br>
	 * 设备ID与其父设备关系
	 */
	private Map<String, List<ObjRela>> deviceParentsMap = new ConcurrentHashMap<String, List<ObjRela>>();
	/**
	 * Map(pointId, ObjRela)</br>
	 * 用于映射obj_rela表的point_id和其所在记录的关系</br>
	 * 业务上是逆变器/汇流箱的直流电流测点与对应组串设备的关系
	 */
	private Map<Long, ObjRela> pointRelaDeviceMap = new ConcurrentHashMap<Long, ObjRela>();
	
	
	@Override
	public int updateDevices(List<Device> deviceList) {
		if (CommonService.isEmpty(deviceList)) return 0;
		
		Device pre = null;
		int psrPreSize = psrDevices.size(), devicePreSize = devices.size();
		int psrUpdate = 0, psrAdd = 0, deviceUpdate = 0, deviceAdd = 0;
		Map<String, Station> stations = stationCacheService.getStations();
		Station station = null;
		for (Device d : deviceList) {
			if (d.getCommuStatus() == null) {
				d.setCommuStatus(OfflineAlarmService.COMMU_STATUS_ONLINE_STR);
			}
			pre = psrDevices.put(d.getPsrId(), d);
			if (pre == null) {
				++psrAdd;
			} else {
				++psrUpdate;
			}
			pre = devices.put(d.getDeviceId(), d);
			if (pre == null) {
				++deviceAdd;
			} else {
				++deviceUpdate;
			}
			
			station = stations.get(d.getStationId());
			if (station != null) {
				if (DeviceService.DEVICE_TYPE_JNB.equals(d.getDeviceType())
				 || DeviceService.DEVICE_TYPE_NB.equals(d.getDeviceType())) {
					station.getInverters().add(d);
				} else if (DeviceService.DEVICE_TYPE_DNB.equals(d.getDeviceType())
						&& DeviceService.USE_FLAG_TOTAL_METER.equals(d.getUseFlag())) {
					station.getMeters().add(d);
				}
			}
		}
		logger.info("psrDevices {}->{}, update: {}, add: {}", psrPreSize, psrDevices.size(), psrUpdate, psrAdd);
		logger.info("devices {}->{}, update: {}, add: {}", devicePreSize, devices.size(), deviceUpdate, deviceAdd);
		return deviceList.size();
	}

	@Override
	public int deleteDevices(List<Device> deviceList) {
		if (CommonService.isEmpty(deviceList)) return 0;
		
		Device pre = null;
		int psrPreSize = psrDevices.size(), devicePreSize = devices.size();
		int psrDelete = 0, deviceDelete = 0;
		for (Device s : deviceList) {
			pre = psrDevices.remove(s.getPsrId());
			if (pre != null) {
				++psrDelete;
			}
			pre = devices.remove(s.getDeviceId());
			if (pre != null) {
				++deviceDelete;
			}
		}
		logger.info("psrDevices {}->{}, delete: {}", psrPreSize, psrDevices.size(), psrDelete);
		logger.info("devices {}->{}, delete: {}", devicePreSize, devices.size(), deviceDelete);
		return deviceList.size();
	}
	
	@Override
	public Device getDeviceByPsrId(String psrId) {
		return psrDevices.get(psrId);
	}
	
	@Override
	public Device getDeviceByDeviceId(String deviceId) {
		return deviceId == null ? null : devices.get(deviceId);
	}
	
	@Override
	public Map<String, Device> getDevices() {
		return devices;
	}

	@Override
	public List<Device> getDevicesByStationId(String stationId, Set<String> deviceTypes) {
		List<Device> list = new ArrayList<Device>();
		for (Device d : devices.values()) {
			if (stationId.equals(d.getStationId())) {
				if (CommonService.isEmpty(deviceTypes)) {
					list.add(d);
				} else if (deviceTypes.contains(d.getDeviceType())) {
					list.add(d);
				}
			}
		}
		return list;
	}

	@Override
	public Map<String, Device> getDevicesByDeviceIds(List<String> deviceIds) {
		Map<String, Device> map = new HashMap<String, Device>();
		for (String did : deviceIds) {
			map.put(did, devices.get(did));
		}
		return map;
	}

	@Override
	public Map<String, Integer> getDeviceCacheSize() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("psrDevices", psrDevices.size());
		map.put("devices", devices.size());
		return map;
	}

	@Override
	public Map<String, Integer> getStationCount() {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Device d : devices.values()) {
			Integer count = map.get(d.getStationId());
			if (count == null) {
				map.put(d.getStationId(), 1);
			} else {
				count += 1;
				map.put(d.getStationId(), count);
			}
		}
		return map;
	}

	@Override
	public Map<String, Integer> getStationDeviceTypeCount(String stationId) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		for (Device d : devices.values()) {
			if (!stationId.equals(d.getStationId())) continue;
			
			Integer count = map.get(d.getDeviceType());
			if (count == null) {
				map.put(d.getDeviceType(), 1);
			} else {
				count += 1;
				map.put(d.getDeviceType(), count);
			}
		}
		return map;
	}

	@Override
	public int updateDeviceRelas(List<ObjRela> relas) {
		if (CommonService.isEmpty(relas)) return 0;
		
		for (ObjRela r : relas) {
			List<ObjRela> childDeviceIds = deviceChildrenMap.get(r.getObjId1());
			if (childDeviceIds == null) {
				childDeviceIds = new ArrayList<ObjRela>();
				deviceChildrenMap.put(r.getObjId1(), childDeviceIds);
			}
			childDeviceIds.add(r);
			
			List<ObjRela> parentDeviceIds = deviceParentsMap.get(r.getObjId2());
			if (parentDeviceIds == null) {
				parentDeviceIds = new ArrayList<ObjRela>();
				deviceParentsMap.put(r.getObjId2(), parentDeviceIds);
			}
			parentDeviceIds.add(r);
			
			if (CommonService.isNotEmpty(r.getPointId())) {
				pointRelaDeviceMap.put(r.getPointId(), r);
			}
		}
		
		return relas.size();
	}

	@Override
	public int deleteDeviceRelasByParentIds(List<String> parentIds) {
		if (CommonService.isEmpty(parentIds)) return 0;
		
		int count = 0;
		List<ObjRela> value = null;
		for (String id : parentIds) {
			if (id == null) continue;
			
			value = deviceChildrenMap.remove(id);
			if (value != null) ++count;
		}
		
		return count;
	}

	@Override
	public int deleteDeviceRelasByChildIds(List<String> childIds) {
		if (CommonService.isEmpty(childIds)) return 0;
		
		int count = 0;
		List<ObjRela> value = null;
		for (String id : childIds) {
			if (id == null) continue;
			
			value = deviceParentsMap.remove(id);
			if (value != null) ++count;
		}
		
		return count;
	}

	@Override
	public int deleteDeviceRelasByPointIds(List<Long> pointIds) {
		if (CommonService.isEmpty(pointIds)) return 0;
		
		int count = 0;
		ObjRela value = null;
		for (Long id : pointIds) {
			if (id == null) continue;
			
			value = pointRelaDeviceMap.remove(id);
			if (value != null) ++count;
		}
		
		return count;
	}

	@Override
	public Page<Device> initDevices(int page, List<Integer> channelIds, List<String> deviceIds, List<String> psrIds, 
			CountDto count) {
		Page<Device> devices = deviceService.getDevices(channelIds, null, null, 
				PageRequest.of(page, deviceBatchNumber, Sort.by(Direction.ASC, "psr_id")));
		
		count.setDeviceCount(devices.getNumberOfElements());
		count.addDeviceAll(devices.getNumberOfElements());
		
		updateDevices(devices.getContent());
		
		getIds(devices, psrIds, deviceIds);
		initObjRelaCache(deviceIds, count);				//初始化组串电流测点与组串设备关系
		
		return devices;
	}

	private void getIds(Page<Device> devices, List<String> psrIds, List<String> deviceIds) {
		psrIds.clear();
		deviceIds.clear();
		if (CommonService.isNotEmpty(devices)) {
			for (Device d : devices) {
				psrIds.add(d.getPsrId());
				deviceIds.add(d.getDeviceId());
			}
		}
	}

	private void initObjRelaCache(List<String> deviceIds, CountDto count) {
		List<ObjRela> relas = objRelaService.getDeviceRelas(deviceIds);
		
		count.setObjRelaCount(relas == null ? 0 : relas.size());
		count.addObjRelaAll(count.getObjRelaCount());
		
		updateDeviceRelas(relas);
	}

	@Override
	public ObjRela getPointRelaDevice(Long pointId) {
		return pointId == null ? null : pointRelaDeviceMap.get(pointId);
	}

	@Override
	public Integer getDeviceChildrenCount() {
		return deviceChildrenMap.size();
	}

	@Override
	public List<ObjRela> getDeviceChildren(String deviceId) {
		return deviceId == null ? null : deviceChildrenMap.get(deviceId);
	}

	@Override
	public Integer getDeviceParentsCount() {
		return deviceParentsMap.size();
	}

	@Override
	public List<ObjRela> getDeviceParents(String deviceId) {
		return deviceId == null ? null : deviceParentsMap.get(deviceId);
	}

}
