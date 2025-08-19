package com.cie.nems.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface PointDataRealRepository extends JpaRepository<PointDataReal, Long> {

	List<PointDataReal> findByPidInOrderByDt(List<Long> pids);

}
