package com.cie.nems.monitor.device;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cie.nems.common.exception.NemsException;

public interface DeviceMonitorService {

	public static final Map<String, Integer> DEVICE_COLUMN_TYPES = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 5131532795283391501L;
		{
			put(DeviceMonitorColumn.monitor_id.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.monitor_date.getName(), Types.DATE);
			put(DeviceMonitorColumn.customer_id.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.station_id.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.device_id.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.real_alarms.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.real_alarms_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.last_alarm_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.uncheck_alarms.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.uncheck_alarms_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.day_alarms.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.commu_status.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.last_data_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.commu_status_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.day_offline_num.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.day_offline_dur.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.run_status.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.day_run_status.getName(), Types.VARCHAR);
			put(DeviceMonitorColumn.run_status_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.run_start_time.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.run_end_time.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.run_times.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.day_stop_num.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.fault_stop_duration.getName(), Types.INTEGER);
			put(DeviceMonitorColumn.update_time.getName(), Types.TIMESTAMP);
			put(DeviceMonitorColumn.value1.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value2.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value3.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value4.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value5.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value6.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value7.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value8.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value9.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value10.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value11.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value12.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value13.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value14.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value15.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value16.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value17.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value18.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value19.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value20.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value21.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value22.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value23.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value24.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value25.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value26.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value27.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value28.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value29.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value30.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value31.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value32.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value33.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value34.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value35.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value36.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value37.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value38.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value39.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value40.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value41.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value42.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value43.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value44.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value45.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value46.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value47.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value48.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value49.getName(), Types.DOUBLE);
			put(DeviceMonitorColumn.value50.getName(), Types.DOUBLE);
		}
	};

	public List<DeviceMonitorReal> getDeviceMonitorReals(List<Integer> channelIds);

	public List<Map<String, Object>> getDeviceMonitorRealMap(List<Integer> channelIds);

	public void save(List<Map<String, Object>> values);

	public void moveRealToHis(Date monitorDate) throws NemsException;

}
