package org.tavall.database.qdrant;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseConfigData;
import org.tavall.database.core.database.IDatabaseType;

public interface IQdrantConfigData extends IDatabaseConfigData {

    @Override
    IDatabaseType<IQdrantDatabase, IQdrantDatabaseBuilder> getDatabaseType();

    @Override
    DatabaseConfigType getConfigType();

    @Override
    boolean isReadOnly();

    String getHost();

    int getPort();

    String getUsername();

    String getPassword();

    String getCollectionName();
}

