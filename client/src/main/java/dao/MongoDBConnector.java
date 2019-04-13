package dao;

import com.iroha10.model.Speciality;
import com.iroha10.model.University;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.stream.Stream;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnector {
  private static final Logger logger = LoggerFactory.getLogger(MongoDBConnector.class);

  private final static String mongoHost = "localhost";
  private final static int mongoPort = 27017;
  private final static String database = "university";
  private final static String SPECIALITY_COLLECTION = "speciality"; // todo: remove
  private final static String UNIVERSITY_COLLECTION = "universities";

  private void initializeCollections() {
    MongoClient client = getClient();
    MongoDatabase db = client.getDatabase(database);
    ArrayList<String> collections = db.listCollectionNames().into(new ArrayList<>());

    String[] expected = new String[]{SPECIALITY_COLLECTION, UNIVERSITY_COLLECTION};

    for (String collection: expected) {
      if (!collections.contains(collection))
        db.createCollection(collection);
    }
  }

  public MongoDBConnector() {
    initializeCollections();
  }

  private MongoClient getClient() {
    logger.debug("Request connection to MongoDB");
    return new MongoClient(mongoHost , mongoPort);
  }

  private MongoDatabase getDB() {
    logger.debug("Request DB={}", database);
    return getClient().getDatabase(database);
  }

  public void insertSpeciality(Speciality spec) {
    insertDoc(SPECIALITY_COLLECTION, spec);
  }

  public void insertUniversity(University uni) {
    insertDoc(UNIVERSITY_COLLECTION, uni);
  }

  private void insertDoc(String collection, Object object) {
    MongoCollection<Document> specialityCollection = getDB().getCollection(collection);
    Document doc = Document.parse(object.toString());
    specialityCollection.insertOne(doc);
  }

  public static void main(String[] args) {
    MongoDBConnector connector = new MongoDBConnector();
    MongoClient client = connector.getClient();
    MongoDatabase mdb = client.getDatabase(UNIVERSITY_COLLECTION);
//    mdb.createCollection("linux");
    MongoCollection<Document> collection = mdb.getCollection(SPECIALITY_COLLECTION);

    Document doc = new Document();
    doc.put("gnu", "1.0");
    doc.put("quantity", 12);

    collection.insertOne(doc);
    long l = collection.countDocuments();
    System.out.println();
  }

}
