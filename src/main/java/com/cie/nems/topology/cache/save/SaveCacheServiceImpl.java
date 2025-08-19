package com.cie.nems.topology.cache.save;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.cie.nems.topology.save.mongo.MongoDocumentDto;

@Service
public class SaveCacheServiceImpl implements SaveCacheService {

	private Map<Long, Long> preSaveDt = new ConcurrentHashMap<Long, Long>();
	private Map<Long, Double> preSaveDv = new ConcurrentHashMap<Long, Double>();
	
	/** Map(pid, Map(dt, value)) */
	private Map<Long, MongoDocumentDto> documents = new ConcurrentHashMap<Long, MongoDocumentDto>();

	@Override
	public Long getPreSaveDt(Long pid) {
		return preSaveDt.get(pid);
	}

	@Override
	public void updatePreSaveDt(Long pid, Long dt) {
		preSaveDt.put(pid, dt);
	}

	@Override
	public Double getPreSaveDv(Long pid) {
		return preSaveDv.get(pid);
	}

	@Override
	public void updatePreSaveDv(Long pid, Double dv) {
		preSaveDv.put(pid, dv);
	}

	@Override
	public MongoDocumentDto getDocument(Long pid) {
		return documents.get(pid);
	}

	@Override
	public void updateDocument(Long pid, MongoDocumentDto doc) {
		documents.put(pid, doc);
	}

}
