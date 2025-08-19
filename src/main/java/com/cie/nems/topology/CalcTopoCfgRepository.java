package com.cie.nems.topology;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CalcTopoCfgRepository extends JpaRepository<CalcTopoCfg, String> {

	List<CalcTopoCfg> findByTopoIpAndTopoAppAndTopoStatus(String ip, String appId, String status);

	List<CalcTopoCfg> findByTopoIpAndTopoApp(String ip, String appId);

}
