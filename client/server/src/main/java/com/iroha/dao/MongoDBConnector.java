package com.iroha.dao;

import static com.iroha.utils.ChainEntitiesUtils.*;
import static com.mongodb.client.model.Filters.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iroha.dao.model.UniversityKeys;
import com.iroha.model.Applicant;
import com.iroha.model.applicant.responses.RegistrationTx;
import com.iroha.model.university.*;
import com.iroha.utils.ChainEntitiesUtils;

import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.*;
import com.mongodb.client.model.Indexes;

import java.io.IOException;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

import jp.co.soramitsu.crypto.ed25519.Ed25519Sha3;
import jp.co.soramitsu.crypto.ed25519.EdDSAPrivateKey;
import jp.co.soramitsu.crypto.ed25519.EdDSAPublicKey;
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
  private final static String UNIVERSITY_KEYS_COLLECTION = "university_keys";
  private final Gson gson = new GsonBuilder().create();

  /**
   * Method makes sure all collections do exist
   * If collection does not exist - creates it
   */
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

  /**
   * Return university from mongodb by name
   * @param uniName
   * @return
   */
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

  /**
   * Method returns specialities from collection by provided code and/or university
   * Both parameters are nullable
   * @param code of the speciality
   * @param university name
   * @return list of specialities fltered
   */
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

  /**
   * Currently not used method
   * @param txHash
   * @return
   */
  @Deprecated
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

  /**
   * Insert university' key into mongo
   * @param uni
   * @param keys
   */
  public void insertUniversityKeys(University uni, KeyPair keys) {
    String encodedPKey = ChainEntitiesUtils.bytesToHex(keys.getPrivate().getEncoded());
    String encodedPubKey = ChainEntitiesUtils.bytesToHex(keys.getPublic().getEncoded());
    Document doc = new Document();
    doc.append("university", uni.getName());
    doc.append("pkey", encodedPKey);
    doc.append("pubkey", encodedPubKey);

    insertDoc(UNIVERSITY_KEYS_COLLECTION, doc);
  }

  /**
   * Method returns university key by name
   * @param name
   * @return
   */
  public KeyPair getUniversityKeys(String name) {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(UNIVERSITY_KEYS_COLLECTION);
      return collection.find(eq("university", name))
          .map(pair -> {
            String encodedPKey = (String) pair.get("pkey");
            String encodedPubKey = (String) pair.get("pubkey");
            byte[] bytesPkey = ChainEntitiesUtils.hexToBytes(encodedPKey);
            byte[] bytesPubKey = ChainEntitiesUtils.hexToBytes(encodedPubKey);
            return Ed25519Sha3.keyPairFromBytes(bytesPkey, bytesPubKey);
          })
          .first();
    }
  }

  /**
   * Return all University+Key objects from mongodb
   * @return
   */
  public List<UniversityKeys> getUniversityKeys() {
    try (MongoClient client = getClient()) {
      MongoCollection<Document> collection = getDB(client).getCollection(UNIVERSITY_KEYS_COLLECTION);
      return collection.find()
          .map(pair -> {
            String name = (String) pair.get("university");
            String encodedPKey = (String) pair.get("pkey");
            String encodedPubKey = (String) pair.get("pubkey");
            byte[] bytesPkey = ChainEntitiesUtils.hexToBytes(encodedPKey);
            byte[] bytesPubKey = ChainEntitiesUtils.hexToBytes(encodedPubKey);
            KeyPair keys = Ed25519Sha3.keyPairFromBytes(bytesPkey, bytesPubKey);

            return new UniversityKeys(name, keys);
          }).into(new ArrayList<>());
    }
  }
}
