package org.tavall.database.mongo;

import org.tavall.database.mongo.connection.IMongoConnectionHandler;
import org.tavall.database.mongo.connection.MongoConnectionHandler;
import org.tavall.database.mongo.exception.MongoDatabaseException;
import org.tavall.database.mongo.query.IMongoQueryHandler;
import org.tavall.database.mongo.query.MongoQueryHandler;
import org.tavall.logging.Log;

import java.util.Optional;

public final class MongoDatabaseBuilder implements IMongoDatabaseBuilder {

    private String host;
    private int port = 27017;
    private String username;
    private String password;
    private boolean readOnly;
    private String databaseName = "tavall";

    private MongoDatabaseBuilder() {
    }

    public static MongoDatabaseBuilder create() {
        return new MongoDatabaseBuilder();
    }

    @Override
    public MongoDatabaseBuilder host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public MongoDatabaseBuilder port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public MongoDatabaseBuilder username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public MongoDatabaseBuilder password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public MongoDatabaseBuilder readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public MongoDatabaseBuilder databaseName(String databaseName) {
        if (databaseName != null && !databaseName.isBlank()) {
            this.databaseName = databaseName;
        }
        return this;
    }

    @Override
    public Optional<IMongoDatabase> build() {
        if (host == null || host.isBlank()) {
            MongoDatabaseException exception = new MongoDatabaseException(
                    "Unable to build Mongo database because host is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (port <= 0) {
            MongoDatabaseException exception = new MongoDatabaseException(
                    "Unable to build Mongo database because port is invalid."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (databaseName == null || databaseName.isBlank()) {
            MongoDatabaseException exception = new MongoDatabaseException(
                    "Unable to build Mongo database because databaseName is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }

        try {
            MongoConfigData configData = new MongoConfigData(
                    host,
                    port,
                    username,
                    password,
                    readOnly,
                    databaseName
            );
            IMongoConnectionHandler connections = new MongoConnectionHandler(configData);
            IMongoQueryHandler queries = new MongoQueryHandler(connections);
            IMongoDatabase database = new MongoDatabase(configData, connections, queries);
            return Optional.of(database);
        } catch (RuntimeException exception) {
            MongoDatabaseException mongoDatabaseException = new MongoDatabaseException(
                    "Unable to build Mongo database.",
                    exception
            );
            Log.exception(mongoDatabaseException);
            return Optional.empty();
        }
    }
}
