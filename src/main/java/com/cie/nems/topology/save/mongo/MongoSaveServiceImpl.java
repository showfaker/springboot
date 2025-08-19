package com.cie.nems.topology.save.mongo;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cie.nems.common.Constants;
import com.cie.nems.common.mongo.MongoService;
import com.cie.nems.common.service.CommonService;
import com.cie.nems.point.PointConstants;
import com.cie.nems.topology.cache.point.value.PointValueDto;
import com.cie.nems.topology.cache.save.SaveCacheService;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.UpdateResult;

@Service
public class MongoSaveServiceImpl implements MongoSaveService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value("${cie.app.debug.save:#{false}}")
	private boolean debug;

	@Value("${cie.save.db-name:#{nems}}")
	private String dbName;
	@Value("${cie.save.monthly-db:#{true}}")
	private Boolean monthlyDb;
	
	@Autowired
	private MongoService mongoService;

	@Autowired
	private SaveCacheService saveCacheService;
	
	private final DateTimeFormatter yyyyMMdd = DateTimeFormatter.ofPattern("yyyyMMdd");
	private final DateTimeFormatter mmssSSS = DateTimeFormatter.ofPattern("mmssSSS");
	
	@Override
	public void save(List<PointValueDto> datas) {
		Map<String, List<MongoSaveDto>> dbDatas = new HashMap<String, List<MongoSaveDto>>();
		String collName = null;
		LocalDateTime time = null;
		long now = System.currentTimeMillis();
		for (PointValueDto data : datas) {
			time = Instant.ofEpochMilli(data.getDt())
					.atZone(ZoneOffset.ofHours(Constants.DEFAULT_ZONE_OFFSET_HOURS))
					.toLocalDateTime();
			
			collName = getCollName(data.getPoint().getDataPeriod(), time);
			List<MongoSaveDto> list = dbDatas.get(collName);
			if (list == null) {
				list = new ArrayList<MongoSaveDto>();
				dbDatas.put(collName, list);
			}
			list.add(getMongoSaveDto(data, time, now));
		}
		
		for (Entry<String, List<MongoSaveDto>> e : dbDatas.entrySet()) {
			// MongoDatabase db = mongoService.getMongoDatabase(getDbName(e.getKey()));
			// MongoCollection<Document> col = db.getCollection(e.getKey());
			MongoCollection<Document> col = mongoService.getCollection(getDbName(e.getKey()), e.getKey());
			
			MongoDocumentDto doc = null;
			List<Long> query_ids = new ArrayList<Long>();
			//List<Long> update_ids = new ArrayList<Long>();
			List<MongoSaveDto> checkList = new ArrayList<MongoSaveDto>();
			Map<Long, MongoDocumentDto> inserts = new HashMap<Long, MongoDocumentDto>();
			List<MongoDocumentDto> updates = new ArrayList<MongoDocumentDto>();
			for(MongoSaveDto data : e.getValue()) {
				doc = saveCacheService.getDocument(data.getPid());
				if (doc == null || doc.get_id() != data.get_id()) {
					//缓存内不存在的有两种情况：
					//1.程序刚刚启动，还没有从数据库中加载数据到内存
					//2.该_id之前一直没有数据
					//所以这批_id要先到数据库查询一次，以便区分是做update还是做insert
					query_ids.add(data.get_id());
					checkList.add(data);
				} else {
					//已存在的数据直接做update
					updateDocument(doc, data);
					updates.add(doc);
					//update_ids.add(doc.get_id());
				}
			}
			
			int checkUpdate = 0;
			if (query_ids.size() > 0) {
				Document query = new Document("_id", new Document("$in", query_ids));
				Map<Long, MongoDocumentDto> mdocs = new HashMap<Long, MongoDocumentDto>();
				for (Document d : col.find(query)) {
					MongoDocumentDto mdoc = getMongoDocumentDto(d);
					mdocs.put(mdoc.get_id(), mdoc);
				}
				for (MongoSaveDto data : checkList) {
					MongoDocumentDto mdoc = mdocs.get(data.get_id());
					if (mdoc == null) {
						//数据库内也不存在，做insert
						mdoc = createDocument(data);
						//如果一个批次中有同一个测点的多个_id，则都会加入到inserts中去插入
						inserts.put(mdoc.get_id(), mdoc);
						//如果一个批次中有同一个测点的多个_id，缓存中只保留最后一个
						saveCacheService.updateDocument(data.getPid(), mdoc);
					} else {
						updateDocument(mdoc, data);
						updates.add(mdoc);
						//update_ids.add(mdoc.get_id());
						saveCacheService.updateDocument(data.getPid(), mdoc);
						++checkUpdate;
					}
				}
			}
			
			if (inserts.size() > 0) {
				col.insertMany(getInsertDocuments(inserts));
				if (debug) {
					logger.debug("insert {} into {}", inserts.size(), e.getKey());
				}
			}
			if (updates.size() > 0) {
//				Document filter = new Document("_id", new Document("$in", update_ids));
//				UpdateResult res = col.updateMany(filter, getUpdateDocuments(updates));
//				if (debug) {
//					logger.debug("update {}, toUpdate: {}({}), matched: {}, modified: {}", e.getKey(), 
//							updates.size(), checkUpdate, res.getMatchedCount(), res.getModifiedCount());
//				}
				int matched = 0, modified = 0;
				for (MongoDocumentDto u : updates) {
					UpdateResult res = col.updateOne(new Document("_id", u.get_id()), 
							new Document("$set", new Document("value", u.getValue().values())));
					matched += res.getMatchedCount();
					modified += res.getModifiedCount();
				}
				if (debug) {
					logger.debug("update {}, toUpdate: {}({}), matched: {}, modified: {}", e.getKey(), 
							updates.size(), checkUpdate, matched, modified);
				}
			}
		}
	}

	private List<? extends Document> getInsertDocuments(Map<Long, MongoDocumentDto> mdocs) {
		List<Document> docs = new ArrayList<Document>();
		for (MongoDocumentDto mdoc : mdocs.values()) {
			docs.add(new Document("_id", mdoc.get_id())
					.append("value", new ArrayList<String>(mdoc.getValue().values())));
		}
		return docs;
	}

//	private List<? extends Document> getUpdateDocuments(List<MongoDocumentDto> mdocs) {
//		List<Document> docs = new ArrayList<Document>();
//		for (MongoDocumentDto mdoc : mdocs) {
//			docs.add(new Document("$set", 
//					new Document("value", new ArrayList<String>(
//							mdoc.getValue().values()
//							))));
//		}
//		return docs;
//	}

	private MongoDocumentDto createDocument(MongoSaveDto data) {
		MongoDocumentDto mdoc = new MongoDocumentDto();
		mdoc.set_id(data.get_id());
		Map<String, String> value = new TreeMap<String, String>();
		value.put(data.getDt(), new StringBuilder(data.getV()).append(',')
									.append(data.getDt()).append(',')
									.append(data.getT()).append(',')
									.append(data.getQ()).toString());
		mdoc.setValue(value);
		return mdoc;
	}

	private void updateDocument(MongoDocumentDto doc, MongoSaveDto data) {
		/*
		{
		  "_id" : NumberLong(37942203),
		  "value" : ["-0.02,2000,-19944131,0", "-0.02,601000,-19944775,0", "-0.02,1102000,-19942684,0", "-0.02,1701000,-19943705,0", "-0.02,2202000,-19945077,0", "-0.02,2801000,-19944043,0", "-0.02,3302000,-19936836,0", "-0.02,3901000,-19937946,0", "-0.02,4402000,-19941910,0", "-0.02,5001000,-19944133,0", "-0.02,5502000,-19943684,0"]
		}
		*/
		Map<String, String> value = doc.getValue();
		if (value == null) {
			value = new TreeMap<String, String>();
			doc.setValue(value);
		}
		value.put(data.getDt(), new StringBuilder(data.getV()).append(',')
									.append(data.getDt()).append(',')
									.append(data.getT()).append(',')
									.append(data.getQ()).toString());
	}

	private MongoDocumentDto getMongoDocumentDto(Document doc) {
		MongoDocumentDto mdoc = new MongoDocumentDto();
		mdoc.set_id(doc.getLong("_id"));
		
		List<String> list = doc.getList("value", String.class);
		if (CommonService.isNotEmpty(list)) {
			Map<String, String> map = new TreeMap<String, String>();
			String[] token = null;
			for (String v : list) {
				token = StringUtils.split(v, ',');
				if (token == null || token.length != 4) continue;
				map.put(token[1], v);
			}
			mdoc.setValue(map);
		}
		
		return mdoc;
	}

	private String getCollName(String dataPeriod, LocalDateTime date) {
		if (PointConstants.POINT_DATA_PERIOD_HOUR.equals(dataPeriod)) {
			return new StringBuffer(NEMS_VALUES_HOUR_PREFIX).append(date.getYear()).toString();
		} else if (PointConstants.POINT_DATA_PERIOD_DAY.equals(dataPeriod)) {
			return NEMS_VALUES_DAY;
		} else if (PointConstants.POINT_DATA_PERIOD_MONTH.equals(dataPeriod)) {
			return NEMS_VALUES_MON;
		} else if (PointConstants.POINT_DATA_PERIOD_YEAR.equals(dataPeriod)) {
			return NEMS_VALUES_YEAR;
		} else {
			return new StringBuffer(NEMS_VALUES_PREFIX).append(date.format(yyyyMMdd)).toString();
		}
	}

	private String getDbName(String collName) {
		if (monthlyDb) {
			if (NEMS_VALUES_YEAR.equals(collName) || NEMS_VALUES_MON.equals(collName)
			 || NEMS_VALUES_DAY.equals(collName) || collName.startsWith(NEMS_VALUES_HOUR_PREFIX)) {
				return dbName;
			} else {
				return dbName + "_" + collName.substring(collName.lastIndexOf('_') + 1, collName.length() - 2);
			}
		} else {
			return dbName;
		}
	}

	private MongoSaveDto getMongoSaveDto(PointValueDto data, LocalDateTime time, long now) {
		MongoSaveDto dto = new MongoSaveDto();
		if (data.getPoint().getDataPeriod().startsWith("0")) {
			dto.set_id(data.getPid() * 100L + time.getHour());
			dto.setDt(time.format(mmssSSS));
		} else if (PointConstants.POINT_DATA_PERIOD_HOUR.equals(data.getPoint().getDataPeriod())) {
			dto.set_id(data.getPid() * 10000L + time.getMonthValue() * 100L + time.getDayOfMonth());
			dto.setDt(String.valueOf(time.getHour()));
		} else if (PointConstants.POINT_DATA_PERIOD_DAY.equals(data.getPoint().getDataPeriod())) {
			dto.set_id(data.getPid() * 10000L + time.getYear() % 100L * 100L + time.getMonthValue());
			dto.setDt(String.valueOf(time.getDayOfMonth()));
		} else if (PointConstants.POINT_DATA_PERIOD_MONTH.equals(data.getPoint().getDataPeriod())) {
			dto.set_id(data.getPid() * 100L + time.getYear() % 100L);
			dto.setDt(String.valueOf(time.getMonthValue()));
		} else if (PointConstants.POINT_DATA_PERIOD_YEAR.equals(data.getPoint().getDataPeriod())) {
			dto.set_id(data.getPid());
			dto.setDt(String.valueOf(time.getYear()));
		} else {
			dto.set_id(data.getPid() * 100L + time.getHour());
			dto.setDt(time.format(mmssSSS));
		}

		if (PointConstants.POINT_DATA_TYPE_DOUBLE.equals(data.getPoint().getDataType())
		 && data.getPoint().getSavePrecision() != null) {
			dto.setV(CommonService.round(data.getDv(), data.getPoint().getSavePrecision()).toString());
		} else {
			dto.setV(data.getV());
		}
		
		dto.setPid(data.getPid());
		dto.setT(data.getDt() - now);
		dto.setQ(data.getQ());
		return dto;
	}

}
