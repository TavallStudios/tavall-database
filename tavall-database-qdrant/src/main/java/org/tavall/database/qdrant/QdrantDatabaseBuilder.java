package org.tavall.database.qdrant;

import org.tavall.database.qdrant.connection.IQdrantConnectionHandler;
import org.tavall.database.qdrant.connection.QdrantConnectionHandler;
import org.tavall.database.qdrant.exception.QdrantDatabaseException;
import org.tavall.database.qdrant.query.IQdrantQueryHandler;
import org.tavall.database.qdrant.query.QdrantQueryHandler;
import org.tavall.logging.Log;

import java.util.Optional;

public final class QdrantDatabaseBuilder implements IQdrantDatabaseBuilder {

    private String host;
    private int port = 6333;
    private String username;
    private String password;
    private boolean readOnly;
    private String collectionName = "tavall_database";

    private QdrantDatabaseBuilder() {
    }

    public static QdrantDatabaseBuilder create() {
        return new QdrantDatabaseBuilder();
    }

    @Override
    public QdrantDatabaseBuilder host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public QdrantDatabaseBuilder port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public QdrantDatabaseBuilder username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public QdrantDatabaseBuilder password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public QdrantDatabaseBuilder readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public QdrantDatabaseBuilder collectionName(String collectionName) {
        if (collectionName != null && !collectionName.isBlank()) {
            this.collectionName = collectionName;
        }
        return this;
    }

    @Override
    public Optional<IQdrantDatabase> build() {
        if (host == null || host.isBlank()) {
            QdrantDatabaseException exception = new QdrantDatabaseException(
                    "Unable to build Qdrant database because host is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (port <= 0) {
            QdrantDatabaseException exception = new QdrantDatabaseException(
                    "Unable to build Qdrant database because port is invalid."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (collectionName == null || collectionName.isBlank()) {
            QdrantDatabaseException exception = new QdrantDatabaseException(
                    "Unable to build Qdrant database because collectionName is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }

        try {
            QdrantConfigData configData = new QdrantConfigData(
                    host,
                    port,
                    username,
                    password,
                    readOnly,
                    collectionName
            );
            IQdrantConnectionHandler connections = new QdrantConnectionHandler(configData);
            IQdrantQueryHandler queries = new QdrantQueryHandler(connections);
            IQdrantDatabase database = new QdrantDatabase(configData, connections, queries);
            return Optional.of(database);
        } catch (RuntimeException exception) {
            QdrantDatabaseException qdrantDatabaseException = new QdrantDatabaseException(
                    "Unable to build Qdrant database.",
                    exception
            );
            Log.exception(qdrantDatabaseException);
            return Optional.empty();
        }
    }
}
