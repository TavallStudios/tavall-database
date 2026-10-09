package org.tavall.database.redis.connection;

import java.util.Optional;
import org.tavall.database.redis.exception.RedisConnectionException;
import redis.clients.jedis.JedisPooled;

/**
 * Jedis-specific connection access retained for consumers that still issue raw client commands.
 *
 * <p>Compatibility boundary: this subtype lives in the concrete provider so the public Redis API stays
 * client-free. Remove it once remaining raw-Jedis consumers (Tavall Cloud, Tavall MC) use the typed
 * {@code IRedisDatabase} capabilities.</p>
 */
public interface IJedisRedisConnectionHandler extends IRedisConnectionHandler {

    /** The handler-owned shared pool; empty after {@link #close()}. Callers must not close it. */
    Optional<JedisPooled> openClient();

    /** The shared pool for typed provider handlers; fails with {@link RedisConnectionException} once closed. */
    default JedisPooled requireClient() {
        return openClient().orElseThrow(() -> new RedisConnectionException("Redis connection handler is closed."));
    }

    /** Retained for source compatibility; the shared pool is owned and closed by this handler. */
    void closeClient(JedisPooled client);
}
