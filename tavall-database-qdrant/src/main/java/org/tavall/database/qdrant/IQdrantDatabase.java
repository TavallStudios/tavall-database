package org.tavall.database.qdrant;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.qdrant.connection.IQdrantConnectionHandler;
import org.tavall.database.qdrant.query.IQdrantQueryHandler;

public interface IQdrantDatabase extends IDatabase {

    @Override
    IDatabaseType<IQdrantDatabase, IQdrantDatabaseBuilder> getDatabaseType();

    @Override
    IQdrantConfigData getConfigData();

    IQdrantConnectionHandler connections();

    @Override
    IQdrantQueryHandler queries();
}
