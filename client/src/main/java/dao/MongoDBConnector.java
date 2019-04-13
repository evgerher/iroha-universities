package dao;

import com.mongodb.MongoClient;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MongoDBConnector {
  private static final Logger logger = LoggerFactory.getLogger(MongoDBConnector.class);

  private final static String mongoHost = "localhost";
  private final static int mongoPort = 27017;

  public MongoClient getClient() {
    logger.debug("Request connection to MongoDB");
    return new MongoClient( mongoHost , mongoPort);
  }

  public MongoDatabase getDB(String db) {
    logger.debug("Request DB={}", db);
    return getClient().getDatabase(db);
  }

  public static void main(String[] args) {
    MongoDBConnector connector = new MongoDBConnector();
    MongoClient client = connector.getClient();
    MongoDatabase mdb = client.getDatabase("GNU_Linux");
    mdb.createCollection("linux");
    MongoCollection<Document> collection = mdb.getCollection("linux");

    Document doc = new Document();

    collection.insertOne(doc);
  }

}
