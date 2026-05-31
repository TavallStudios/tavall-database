package org.tavall.database.mongo.connection;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.tavall.database.mongo.IMongoConfigData;
import org.tavall.database.mongo.exception.MongoConnectionException;
import org.tavall.logging.Log;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class MongoConnectionHandler implements IMongoConnectionHandler {

    private final IMongoConfigData configData;
    private final MongoClient client;
    private final MongoDatabase database;
    private boolean closed;

    public MongoConnectionHandler(IMongoConfigData configData) {
        this.configData = configData;
        String connectionString = buildConnectionString(configData);
        this.client = MongoClients.create(new ConnectionString(connectionString));
        this.database = client.getDatabase(configData.getDatabaseName());
    }

    @Override
    public Optional<MongoClient> openClient() {
        if (closed) {
            return Optional.empty();
        }
        return Optional.of(client);
    }

    @Override
    public Optional<MongoDatabase> openDatabase() {
        if (closed) {
            return Optional.empty();
        }
        return Optional.of(database);
    }

    @Override
    public void closeClient(MongoClient client) {
        if (client == null) {
            return;
        }

        try {
            client.close();
        } catch (RuntimeException exception) {
            MongoConnectionException mongoConnectionException = new MongoConnectionException(
                    "Unable to close Mongo client.",
                    exception
            );
            Log.exception(mongoConnectionException);
        }
    }

    @Override
    public boolean isAvailable() {
        if (closed) {
            return false;
        }

        try {
            database.runCommand(new Document("ping", 1));
            return true;
        } catch (RuntimeException exception) {
            MongoConnectionException mongoConnectionException = new MongoConnectionException(
                    "Unable to validate Mongo connection.",
                    exception
            );
            Log.exception(mongoConnectionException);
            return false;
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        closeClient(client);
    }

    private String buildConnectionString(IMongoConfigData configData) {
        StringBuilder connectionString = new StringBuilder("mongodb://");
        String username = configData.getUsername();
        String password = configData.getPassword();
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasPassword = password != null && !password.isBlank();

        if (hasUsername || hasPassword) {
            if (hasUsername) {
                connectionString.append(URLEncoder.encode(username, StandardCharsets.UTF_8));
            }
            if (hasPassword) {
                connectionString.append(':').append(URLEncoder.encode(password, StandardCharsets.UTF_8));
            } else if (hasUsername) {
                connectionString.append(':');
            }
            connectionString.append('@');
        }

        connectionString.append(configData.getHost())
                .append(':')
                .append(configData.getPort())
                .append('/')
                .append(configData.getDatabaseName());
        return connectionString.toString();
    }
}
