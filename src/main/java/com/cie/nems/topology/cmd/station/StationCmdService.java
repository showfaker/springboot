package com.cie.nems.topology.cmd.station;

import com.cie.nems.topology.cmd.CmdDto;

public interface StationCmdService {

	public void updateStationInfo(CmdDto cmd);

	public void deleteStationInfo(CmdDto cmd);

}
