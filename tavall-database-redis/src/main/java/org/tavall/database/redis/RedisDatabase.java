package org.tavall.database.redis;

import java.util.Objects;
import org.tavall.database.core.database.AbstractDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;
import org.tavall.database.redis.connection.RedisConnectionHandler;
import org.tavall.database.redis.lease.IRedisLeaseHandler;
import org.tavall.database.redis.lease.RedisLeaseHandler;
import org.tavall.database.redis.query.IRedisQueryHandler;
import org.tavall.database.redis.query.RedisQueryHandler;
import org.tavall.database.redis.record.IRedisVersionedRecordHandler;
import org.tavall.database.redis.record.RedisVersionedRecordHandler;

/**
 * Jedis-backed Redis database. Owns its connection pool and the typed capability handlers composed over it;
 * {@link #close()} closes the pool, after which every capability fails with {@code RedisConnectionException}.
 */
public final class RedisDatabase extends AbstractDatabase<IRedisConfigData> implements IJedisRedisDatabase {

    private final IJedisRedisConnectionHandler connections;
    private final IRedisQueryHandler queries;
    private final IRedisLeaseHandler leases;
    private final IRedisVersionedRecordHandler records;

    /** Composition performed by {@link RedisDatabaseBuilder}; the handlers share this database's pool. */
    RedisDatabase(IRedisConfigData configData, RedisConnectionHandler connections) {
        this(configData, connections, new RedisQueryHandler(connections));
    }

    /**
     * Explicit composition for callers that supply their own connection and query handlers, such as tests.
     * Lease and record handlers are composed over the supplied connection handler.
     */
    public RedisDatabase(IRedisConfigData configData, IJedisRedisConnectionHandler connections, IRedisQueryHandler queries) {
        super(RedisDatabaseType.REDIS, configData, queries);
        this.connections = Objects.requireNonNull(connections, "connections");
        this.queries = Objects.requireNonNull(queries, "queries");
        this.leases = new RedisLeaseHandler(connections);
        this.records = new RedisVersionedRecordHandler(connections);
    }

    @Override
    public IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> getDatabaseType() {
        return RedisDatabaseType.REDIS;
    }

    @Override
    public IRedisConfigData getConfigData() {
        return super.getConfigData();
    }

    @Override
    public IJedisRedisConnectionHandler connections() {
        return connections;
    }

    @Override
    public IRedisQueryHandler queries() {
        return queries;
    }

    @Override
    public IRedisLeaseHandler leases() {
        return leases;
    }

    @Override
    public IRedisVersionedRecordHandler records() {
        return records;
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
