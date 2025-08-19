package com.cie.nems.topology.cmd.device;

import com.cie.nems.topology.cmd.CmdDto;

public interface DeviceCmdService {

	public void updateDeviceInfo(CmdDto cmd);

	public void deleteDeviceInfo(CmdDto cmd);

}
