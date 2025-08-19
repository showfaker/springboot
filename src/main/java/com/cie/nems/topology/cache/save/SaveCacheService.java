package com.cie.nems.topology.cache.save;

import com.cie.nems.topology.save.mongo.MongoDocumentDto;

public interface SaveCacheService {

	public Long getPreSaveDt(Long pid);

	public void updatePreSaveDt(Long pid, Long dt);

	public Double getPreSaveDv(Long pid);

	public void updatePreSaveDv(Long pid, Double dv);

	public MongoDocumentDto getDocument(Long pid);

	public void updateDocument(Long pid, MongoDocumentDto doc);

}
