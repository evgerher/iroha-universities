package dao;

import static com.mongodb.client.model.Filters.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.iroha10.model.Speciality;
import com.iroha10.model.University;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;

import java.util.ArrayList;
import java.util.Collection;
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
  private final static MongoClient client = getClient();

  private void initializeCollections() {
    MongoDatabase db = client.getDatabase(database);
    ArrayList<String> collections = db.listCollectionNames().into(new ArrayList<>());

    String[] expected = new String[]{SPECIALITY_COLLECTION, UNIVERSITY_COLLECTION};

    for (String collection: expected) {
      if (!collections.contains(collection)) {
        db.createCollection(collection);
        db.getCollection(collection).createIndex(Indexes.ascending("name"));
      }
    }
  }

  public MongoDBConnector() {
    initializeCollections();
  }

  private static MongoClient getClient() {
    logger.debug("Request connection to MongoDB");
    return new MongoClient(mongoHost , mongoPort);
  }

  private MongoDatabase getDB() {
    logger.debug("Request DB={}", database);
    return client.getDatabase(database);
  }

  public void insertSpeciality(String universityName, Speciality spec) {
    MongoCollection<Document> uniCollection = getDB().getCollection(UNIVERSITY_COLLECTION);
    BasicDBObject query = new BasicDBObject();
    query.put("name", universityName);

    Document uni = uniCollection.find(query).first();
    String json = uni.toJson();
    University university = new GsonBuilder().create().fromJson(json, University.class);
    university.addSpeciality(spec);

    Bson filter = eq("name", universityName);
    BasicDBObject updateFields = new BasicDBObject();
    updateFields.append("specialities", university.specialitiesToString());
    BasicDBObject setQuery = new BasicDBObject();
    setQuery.append("$set", updateFields);
    uniCollection.updateOne(filter, setQuery);

  }

  private Document convertJsonToDoc(String json) {
    return Document.parse(json);
  }

  public void insertUniversity(University uni) {
    insertDoc(UNIVERSITY_COLLECTION, uni);
  }

  public Collection<University> getUniversities() {
    Gson gson = new GsonBuilder().create();

    MongoCollection<Document> collection = getDB().getCollection(UNIVERSITY_COLLECTION);

    List<University> unis = new ArrayList<>();
    return collection.find().map(doc -> {
      String json = doc.toJson();
      return gson.fromJson(json, University.class);
    }).into(new ArrayList<University>());

//    try (MongoCursor<Document> cursor = collection.find().iterator()) {
//      String json = cursor.next().toJson();
//      unis.add(gson.fromJson(json, University.class));
//    }
//    return unis;
//
//    return Stream.generate(collection.find().iterator()::next)
//      .map(doc -> {
//          String json = doc.toJson();
//          return gson.fromJson(json, University.class);
//        }).collect(Collectors.toList());
  }

  private void insertDoc(String collectionName, Object object) {
    MongoCollection<Document> collection = getDB().getCollection(collectionName);
    Document doc = Document.parse(object.toString());
    collection.insertOne(doc);
  }

  public static void main(String[] args) {
    MongoDBConnector connector = new MongoDBConnector();
//    MongoClient client = connector.getClient();
//    MongoDatabase mdb = client.getDatabase(database);
//    MongoCollection<Document> universities =  mdb.getCollection(UNIVERSITY_COLLECTION);
    University uni = new University("SPB", new ArrayList<>());
    connector.insertUniversity(uni);
    Collection<University> universities1 = connector.getUniversities();

//    Speciality spec = new Speciality("msit", "lorem ipsum", "12.33.520", 120);
//    connector.insertSpeciality("SPB", spec);

    Collection<University> universities2 = connector.getUniversities();

    System.out.println();
  }

}
