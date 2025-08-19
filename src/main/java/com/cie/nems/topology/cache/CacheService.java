package com.cie.nems.topology.cache;

import com.cie.nems.common.exception.NemsException;

public interface CacheService {

	public void initLocalCache() throws NemsException;

	public boolean isCacheInited();

}
