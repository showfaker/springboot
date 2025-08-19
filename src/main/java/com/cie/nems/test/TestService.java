package com.cie.nems.test;

import java.util.Date;
import java.util.List;

public interface TestService {

	public void deleteDatas(List<Integer> channelIds);

	public Integer kafkaSendToDistr(List<Long> pointIds, Integer offset, Integer limit) throws Exception;

	public void test() throws Exception;

	public void createMonitorRealData(Date date);

}
