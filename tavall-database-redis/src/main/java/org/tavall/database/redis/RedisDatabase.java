package org.tavall.database.redis;

import org.tavall.database.core.database.AbstractDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.redis.connection.IRedisConnectionHandler;
import org.tavall.database.redis.query.IRedisQueryHandler;

import java.util.Objects;

public final class RedisDatabase extends AbstractDatabase<IRedisConfigData> implements IRedisDatabase {

    private final IRedisConnectionHandler connections;
    private final IRedisQueryHandler queries;

    public RedisDatabase(
            IRedisConfigData configData,
            IRedisConnectionHandler connections,
            IRedisQueryHandler queries
    ) {
        super(RedisDatabaseType.REDIS, configData, queries);
        this.connections = Objects.requireNonNull(connections, "connections");
        this.queries = Objects.requireNonNull(queries, "queries");
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
    public IRedisConnectionHandler connections() {
        return connections;
    }

    @Override
    public IRedisQueryHandler queries() {
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
