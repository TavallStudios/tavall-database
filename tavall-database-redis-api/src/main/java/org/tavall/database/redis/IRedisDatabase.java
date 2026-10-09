package org.tavall.database.redis;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.redis.connection.IRedisConnectionHandler;
import org.tavall.database.redis.lease.IRedisLeaseHandler;
import org.tavall.database.redis.query.IRedisQueryHandler;
import org.tavall.database.redis.record.IRedisVersionedRecordHandler;

/**
 * Provider-neutral Redis database. Exposes typed string operations, expiring leases, and fenced versioned
 * records; no concrete client type is reachable from this contract.
 */
public interface IRedisDatabase extends IDatabase {

    @Override
    IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> getDatabaseType();

    @Override
    IRedisConfigData getConfigData();

    IRedisConnectionHandler connections();

    @Override
    IRedisQueryHandler queries();

    IRedisLeaseHandler leases();

    IRedisVersionedRecordHandler records();
}

