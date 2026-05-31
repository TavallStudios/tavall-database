package org.tavall.database.mongo;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseConfigData;
import org.tavall.database.core.database.IDatabaseType;

public interface IMongoConfigData extends IDatabaseConfigData {

    @Override
    IDatabaseType<IMongoDatabase, IMongoDatabaseBuilder> getDatabaseType();

    @Override
    DatabaseConfigType getConfigType();

    @Override
    boolean isReadOnly();

    String getHost();

    int getPort();

    String getUsername();

    String getPassword();

    String getDatabaseName();
}
