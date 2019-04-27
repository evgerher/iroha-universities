package dao;

import static com.mongodb.client.model.Filters.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iroha10.model.university.Speciality;
import com.iroha10.model.university.University;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;

import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnector {
  private static final Logger logger = LoggerFactory.getLogger(MongoDBConnector.class);

  private final static String mongoHost = "localhost";
  private final static int mongoPort = 27017;
  private final static String database = "university";
  private final static String SPECIALITY_COLLECTION = "speciality"; // todo: remove
  private final static String UNIVERSITY_COLLECTION = "universities";
  private final Gson gson = new GsonBuilder().create();

  private void initializeCollections() {
    try (MongoClient client = getClient()) {
      MongoDatabase db = getDB(client);
      ArrayList<String> collections = db.listCollectionNames().into(new ArrayList<>());

      String[] expected = new String[]{SPECIALITY_COLLECTION, UNIVERSITY_COLLECTION};

      for (String collection : expected) {
        if (!collections.contains(collection)) {
          db.createCollection(collection);
          db.getCollection(collection).createIndex(Indexes.ascending("name"));
        }
      }
    }
  }

  public MongoDBConnector() {
    initializeCollections();
  }

  private MongoClient getClient() {
    logger.debug("Request connection to MongoDB");
    return new MongoClient(mongoHost , mongoPort);
  }

  private MongoDatabase getDB(MongoClient client) {
    logger.debug("Request DB={}", database);
    return client.getDatabase(database);
  }

  public void insertSpeciality(Speciality spec) {
    insertDoc(SPECIALITY_COLLECTION, spec);
  }

  public void insertUniversity(University uni) {
    insertDoc(UNIVERSITY_COLLECTION, uni);
  }

  public List<University> getUniversities() {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(UNIVERSITY_COLLECTION);

      return collection.find().map(doc -> {
        String json = doc.toJson();
        return gson.fromJson(json, University.class);
      }).into(new ArrayList<University>());
    }
  }

  public List<Speciality> getSpecialities() {
    return getSpecialities(null, null);
  }

  private void insertDoc(String collectionName, Object object) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(collectionName);
      Document doc = Document.parse(object.toString());
      collection.insertOne(doc);
    }
  }

  public University getUniversity(String uniName) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(UNIVERSITY_COLLECTION);
      return collection.find(eq("name", uniName)).map(doc -> {
        String json = doc.toJson();
        return gson.fromJson(json, University.class);
      }).first();
    }
  }

  private boolean parameterIsValid(String param) {
    return param != null && !param.isEmpty();
  }

  public List<Speciality> getSpecialities(String code, String university) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(SPECIALITY_COLLECTION);

      FindIterable<Document> it;
      if (parameterIsValid(university) || parameterIsValid(code)) {
        Bson filter;
        if (parameterIsValid(university) && parameterIsValid(code))
          filter = and(eq("code", code), eq("university", university));
        else if (parameterIsValid(code))
          filter = eq("code", code);
        else
          filter = eq("university", university);

        it = collection.find(filter);
      } else
        it = collection.find();


      return it.map(doc -> {
        String json = doc.toJson();
        return gson.fromJson(json, Speciality.class);
      }).into(new ArrayList<>());
    }
  }
}
