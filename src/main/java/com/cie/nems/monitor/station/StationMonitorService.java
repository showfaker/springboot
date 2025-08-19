package com.cie.nems.monitor.station;

import java.sql.Types;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cie.nems.common.exception.NemsException;

public interface StationMonitorService {

	public static final Map<String, Integer> STATION_COLUMN_TYPES = new HashMap<String, Integer>() {
		private static final long serialVersionUID = 4312571244693510219L;
		{
			put(StationMonitorColumn.monitor_id.getName(), Types.VARCHAR);
			put(StationMonitorColumn.monitor_date.getName(), Types.DATE);
			put(StationMonitorColumn.customer_id.getName(), Types.VARCHAR);
			put(StationMonitorColumn.station_id.getName(), Types.VARCHAR);
			put(StationMonitorColumn.health_total.getName(), Types.INTEGER);
			put(StationMonitorColumn.health1.getName(), Types.INTEGER);
			put(StationMonitorColumn.health2.getName(), Types.INTEGER);
			put(StationMonitorColumn.health3.getName(), Types.INTEGER);
			put(StationMonitorColumn.health4.getName(), Types.INTEGER);
			put(StationMonitorColumn.health5.getName(), Types.INTEGER);
			put(StationMonitorColumn.energy_input.getName(), Types.DOUBLE);
			put(StationMonitorColumn.energy_coll.getName(), Types.DOUBLE);
			put(StationMonitorColumn.capacity.getName(), Types.DOUBLE);
			put(StationMonitorColumn.run_start_time.getName(), Types.INTEGER);
			put(StationMonitorColumn.run_end_time.getName(), Types.INTEGER);
			put(StationMonitorColumn.run_times.getName(), Types.INTEGER);
			put(StationMonitorColumn.power.getName(), Types.DOUBLE);
			put(StationMonitorColumn.max_power.getName(), Types.DOUBLE);
			put(StationMonitorColumn.max_power_time.getName(), Types.TIMESTAMP);
			put(StationMonitorColumn.run_status.getName(), Types.VARCHAR);
			put(StationMonitorColumn.day_run_status.getName(), Types.VARCHAR);
			put(StationMonitorColumn.update_time.getName(), Types.TIMESTAMP);
			put(StationMonitorColumn.commu_status.getName(), Types.VARCHAR);
		}
	};

	public List<StationMonitorReal> getStationMonitorReals(List<Integer> channelIds);

	public List<Map<String, Object>> getStationMonitorRealMap(List<Integer> channelIds);

	public void save(List<Map<String, Object>> values);

	public void moveRealToHis(Date monitorDate) throws NemsException;

}
