package org.tavall.database.mongo.connection;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;

import java.util.Optional;

public interface IMongoConnectionHandler extends AutoCloseable {

    Optional<MongoClient> openClient();

    Optional<MongoDatabase> openDatabase();

    void closeClient(MongoClient client);

    boolean isAvailable();

    @Override
    void close();
}

