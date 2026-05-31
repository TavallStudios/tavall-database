package org.tavall.database.redis;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.redis.connection.IRedisConnectionHandler;
import org.tavall.database.redis.query.IRedisQueryHandler;

public interface IRedisDatabase extends IDatabase {

    @Override
    IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> getDatabaseType();

    @Override
    IRedisConfigData getConfigData();

    IRedisConnectionHandler connections();

    @Override
    IRedisQueryHandler queries();
}
