package com.cie.nems.point;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cie.nems.common.service.CommonService;

@Service
public class PointInfoServiceImpl implements PointInfoService {

	@Autowired
	private PointInfoDao pointInfoDao;

	@Override
	public List<PointInfoDto> getDevicePoints(List<String> psrIds, boolean getAll) {
		if (CommonService.isEmpty(psrIds)) return null;
		return pointInfoDao.getDevicePoints(psrIds, getAll);
	}

	@Override
	public List<PointInfoDto> getStationPoints(List<Integer> channelIds) {
		if (CommonService.isEmpty(channelIds)) return null;
		return pointInfoDao.getStationPoints(channelIds);
	}

	@Override
	public boolean isYxPoint(PointInfoDto point) {
		if (point == null) return false;
		if (PointConstants.POINT_REMOTION_TYPE_YX.equals(point.getRemotionType())
		 || PointConstants.POINT_REMOTION_TYPE_YK.equals(point.getRemotionType())) {
			return true;
		}
		return false;
	}

}
