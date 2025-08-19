package com.cie.nems.common.mongo;

import org.bson.Document;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public interface MongoService {

	public MongoClient getMongoClient();

	public MongoDatabase getMongoDatabase(String dbname);

	public MongoCollection<Document> getCollection(String dbName, String collectionName);

}
