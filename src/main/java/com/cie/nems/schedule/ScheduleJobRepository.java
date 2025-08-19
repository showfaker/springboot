package com.cie.nems.schedule;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ScheduleJobRepository extends JpaRepository<ScheduleJob, String> {

	List<ScheduleJob> findByJobIpAndJobAppAndJobStatus(String ip, String appId, String status);

}
