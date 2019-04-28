package com.iroha.dao;

import static com.mongodb.client.model.Filters.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iroha.model.Applicant;
import com.iroha.model.applicant.responses.RegistrationTx;
import com.iroha.model.university.Speciality;
import com.iroha.model.university.University;

import com.iroha.utils.ChainEntitiesUtils;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class MongoDBConnector {
  private static final Logger logger = LoggerFactory.getLogger(MongoDBConnector.class);
  static {
    initializeCollections();
  }

  private final static String mongoHost = "localhost";
  private final static int mongoPort = 27017;
  private final static String database = "university";
  private final static String SPECIALITY_COLLECTION = "speciality";
  private final static String UNIVERSITY_COLLECTION = "universities";
  private final static String APPLICANTS_COLLECTION = "applicants";
  private final static String REGISTRATION_COLLECTION = "registration";
  private final static String UNIVERSITY_KEYS_COLLECTION = "registration";
  private final Gson gson = new GsonBuilder().create();

  private static void initializeCollections() {
    try (MongoClient client = new MongoClient(mongoHost , mongoPort)) {
      MongoDatabase db = client.getDatabase(database);
      ArrayList<String> collections = db.listCollectionNames().into(new ArrayList<>());

      String[] expected = new String[]{SPECIALITY_COLLECTION, UNIVERSITY_COLLECTION, APPLICANTS_COLLECTION,
          REGISTRATION_COLLECTION, UNIVERSITY_KEYS_COLLECTION};

      for (String collection : expected) {
        if (!collections.contains(collection)) {
          db.createCollection(collection);
          db.getCollection(collection).createIndex(Indexes.ascending("name"));
        }
      }
    }
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

      return collection.find()
          .map(jsonToObject(University.class))
          .into(new ArrayList<University>());
    }
  }

  public List<Speciality> getSpecialities() {
    return getSpecialities(null, null);
  }

  private void insertDoc(String collectionName, Object object) {
    Document doc = Document.parse(object.toString());
    insertDoc(collectionName, doc);
  }

  private void insertDoc(String collectionName, Document doc) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(collectionName);
      collection.insertOne(doc);
    }
  }

  public University getUniversity(String uniName) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(UNIVERSITY_COLLECTION);
      return collection.find(eq("name", uniName))
          .map(jsonToObject(University.class))
          .first();
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


      return it
          .map(jsonToObject(Speciality.class))
          .into(new ArrayList<>());
    }
  }

  private <T> Function<Document, T> jsonToObject(Class<T> clazz) {
    return doc -> {
      String json = doc.toJson();
      return gson.fromJson(json, clazz);
    };
  }


  public void insertApplicant(Applicant applicant) {
    insertDoc(APPLICANTS_COLLECTION, applicant);
  }

  public Applicant getApplicant(String usercode) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(APPLICANTS_COLLECTION);
      return collection.find(eq("userCode", usercode))
          .map(jsonToObject(Applicant.class))
          .first();
    }
  }

  public void insertRegistrationMapping(RegistrationTx registration) {
    insertDoc(REGISTRATION_COLLECTION, Document.parse(registration.toString()));
  }

  public RegistrationTx getRegistrationMapping(String txHash) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(REGISTRATION_COLLECTION);
      Document doc = collection.findOneAndDelete(eq("txHash", txHash));
      return jsonToObject(doc.toJson(), RegistrationTx.class);
    } catch (Exception e) {
      logger.error("Not found by tx={}, error={}", txHash, e);
      throw e;
    }
  }

  private <T> T jsonToObject(String json, Class<T> targetClass) {
    return gson.fromJson(json, targetClass);
  }

  public void insertUniversityKeys(University uni, KeyPair keys) {
    try (MongoClient client = getClient()) {
      String encodedKeys = encodeKeyPair(keys);
      Document doc = new Document();
      doc.append("university", uni.getName());
      doc.append("keypair", encodedKeys);
      insertDoc(UNIVERSITY_KEYS_COLLECTION, doc);
    } catch (IOException e) {
      //todo: me
    }
  }

  public KeyPair getUniversityKeys(String name) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(UNIVERSITY_KEYS_COLLECTION);
      return collection.find(eq("university", name))
          .map(pair -> {
            String encodedKeyPair = (String) pair.get("keypair");
            byte[] bytes = ChainEntitiesUtils.hexToBytes(encodedKeyPair);
            return decodeKeyPair(bytes);
          })
          .first();
    }
  }

  private String encodeKeyPair(KeyPair keys) throws IOException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
      try (ObjectOutputStream ous = new ObjectOutputStream(baos)) {
        ous.writeObject(keys);
        byte[] bytes = baos.toByteArray();
        return ChainEntitiesUtils.bytesToHex(bytes);
      }
    } catch (IOException e) {
      logger.error("Exception during key storing, {}", e);
      throw e;
    }
  }

  private KeyPair decodeKeyPair(byte[] bytes) {
    try (ByteArrayInputStream bi = new ByteArrayInputStream(bytes)) {
      try (ObjectInputStream oi = new ObjectInputStream(bi)) {
        Object obj = oi.readObject();
        return (KeyPair) obj;
      }
    } catch (Exception e) {
      logger.error("Unable to parse class from byte object");
      return null;
    }
  }
}
