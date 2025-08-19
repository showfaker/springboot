package com.cie.nems.point;

import java.util.List;

public interface PointInfoService {

	public List<PointInfoDto> getDevicePoints(List<String> psrIds, boolean getAll);

	public List<PointInfoDto> getStationPoints(List<Integer> channelIds);

	public boolean isYxPoint(PointInfoDto point);

}
