package org.tavall.database.redis;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseConfigData;
import org.tavall.database.core.database.IDatabaseType;

public interface IRedisConfigData extends IDatabaseConfigData {

    @Override
    IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> getDatabaseType();

    @Override
    DatabaseConfigType getConfigType();

    @Override
    boolean isReadOnly();

    String getHost();

    int getPort();

    String getUsername();

    String getPassword();

    int getDatabaseIndex();
}

