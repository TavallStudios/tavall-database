package org.tavall.database.qdrant;

import org.tavall.database.core.database.AbstractDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.qdrant.connection.IQdrantConnectionHandler;
import org.tavall.database.qdrant.query.IQdrantQueryHandler;

import java.util.Objects;

public final class QdrantDatabase extends AbstractDatabase<IQdrantConfigData> implements IQdrantDatabase {

    private final IQdrantConnectionHandler connections;
    private final IQdrantQueryHandler queries;

    public QdrantDatabase(
            IQdrantConfigData configData,
            IQdrantConnectionHandler connections,
            IQdrantQueryHandler queries
    ) {
        super(QdrantDatabaseType.QDRANT, configData, queries);
        this.connections = Objects.requireNonNull(connections, "connections");
        this.queries = Objects.requireNonNull(queries, "queries");
    }

    @Override
    public IDatabaseType<IQdrantDatabase, IQdrantDatabaseBuilder> getDatabaseType() {
        return QdrantDatabaseType.QDRANT;
    }

    @Override
    public IQdrantConfigData getConfigData() {
        return super.getConfigData();
    }

    @Override
    public IQdrantConnectionHandler connections() {
        return connections;
    }

    @Override
    public IQdrantQueryHandler queries() {
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
