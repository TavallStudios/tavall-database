package org.tavall.database.mongo;

import org.tavall.database.core.database.AbstractDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.mongo.connection.IMongoConnectionHandler;
import org.tavall.database.mongo.query.IMongoQueryHandler;

import java.util.Objects;

public final class MongoDatabase extends AbstractDatabase<IMongoConfigData> implements IMongoDatabase {

    private final IMongoConnectionHandler connections;
    private final IMongoQueryHandler queries;

    public MongoDatabase(
            IMongoConfigData configData,
            IMongoConnectionHandler connections,
            IMongoQueryHandler queries
    ) {
        super(MongoDatabaseType.MONGO, configData, queries);
        this.connections = Objects.requireNonNull(connections, "connections");
        this.queries = Objects.requireNonNull(queries, "queries");
    }

    @Override
    public IDatabaseType<IMongoDatabase, IMongoDatabaseBuilder> getDatabaseType() {
        return MongoDatabaseType.MONGO;
    }

    @Override
    public IMongoConfigData getConfigData() {
        return super.getConfigData();
    }

    @Override
    public IMongoConnectionHandler connections() {
        return connections;
    }

    @Override
    public IMongoQueryHandler queries() {
        return queries;
    }

    @Override
    public boolean isAvailable() {
        return connections.isAvailable();
    }

    @Override
    public void close() {
        connections.close();
    }
}

