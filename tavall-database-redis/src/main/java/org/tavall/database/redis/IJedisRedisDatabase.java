package org.tavall.database.redis;

import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;

/**
 * Jedis-backed {@link IRedisDatabase} whose connection handler also exposes the raw client.
 * Compatibility boundary for raw-client consumers; see {@link IJedisRedisConnectionHandler}.
 */
public interface IJedisRedisDatabase extends IRedisDatabase {

    @Override
    IJedisRedisConnectionHandler connections();
}
