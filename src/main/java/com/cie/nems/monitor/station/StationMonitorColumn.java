package com.cie.nems.monitor.station;

public enum StationMonitorColumn {
	monitor_id("monitor_id"),
	monitor_date("monitor_date"),
	customer_id("customer_id"),
	station_id("station_id"),
	health_total("health_total"),
	health1("health1"),
	health2("health2"),
	health3("health3"),
	health4("health4"),
	health5("health5"),
	energy_input("energy_input"),
	energy_coll("energy_coll"),
	capacity("capacity"),
	run_start_time("run_start_time"),
	run_end_time("run_end_time"),
	run_times("run_times"),
	power("power"),
	max_power("max_power"),
	max_power_time("max_power_time"),
	irradiation("irradiation"),
	theoretic_energy("theoretic_energy"),
	run_status("run_status"),
	day_run_status("day_run_status"),
	update_time("update_time"),
	commu_status("commu_status");

	private final String name;
	private StationMonitorColumn(String name) {
		this.name = name;
	}
	public String getName() {
		return this.name;
	}
}
