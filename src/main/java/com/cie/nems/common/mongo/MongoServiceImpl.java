package com.cie.nems.common.mongo;

import java.util.HashMap;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@Service
public class MongoServiceImpl implements MongoService {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Value(value = "${spring.data.mongodb.uri:#{null}}")
	private String uri;

	private MongoClient mongoClient = null;
	@Override
	public MongoClient getMongoClient() {
		if (mongoClient == null) {
			logger.debug("mongodb connection: {}", uri);
			
			mongoClient = MongoClients.create(uri);
		}
		return mongoClient;
	}

	Map<String, MongoDatabase> mongoDatabases = new HashMap<>();
	@Override
	public MongoDatabase getMongoDatabase(String dbname) {
		MongoDatabase db = mongoDatabases.get(dbname);
		if (db == null) {
			db = getMongoClient().getDatabase(dbname);
			mongoDatabases.put(dbname, db);
		}
		return db;
	}

	Map<String, Map<String, MongoCollection<Document>>> dbColMap = new HashMap<>();
	@Override
	public MongoCollection<Document> getCollection(String dbName, String collectionName) {
		MongoCollection<Document> col = null;
		Map<String, MongoCollection<Document>> colMap = dbColMap.get(dbName);
		if (colMap == null) {
			MongoDatabase db = getMongoDatabase(dbName);
			col = db.getCollection(collectionName);
			
			colMap = new HashMap<>();
			colMap.put(collectionName, col);
			dbColMap.put(dbName, colMap);
		} else {
			col = colMap.get(collectionName);
			if (col == null) {
				MongoDatabase db = getMongoDatabase(dbName);
				col = db.getCollection(collectionName);
				colMap.put(collectionName, col);
			}
		}
		
		return col;
	}

}
