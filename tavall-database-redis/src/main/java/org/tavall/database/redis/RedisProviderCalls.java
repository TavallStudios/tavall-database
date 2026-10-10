package org.tavall.database.redis;

import java.util.function.Supplier;
import org.tavall.database.redis.exception.RedisConnectionException;
import org.tavall.database.redis.exception.RedisQueryException;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

/** Maps Jedis failures onto the typed Redis API exceptions so no client exception escapes the API. */
public final class RedisProviderCalls {

    private RedisProviderCalls() {
    }

    public static <T> T call(Supplier<T> operation) {
        try {
            return operation.get();
        } catch (JedisConnectionException | IllegalStateException exception) {
            // IllegalStateException is how the pool reports use after close; both are connection failures.
            throw new RedisConnectionException("Redis connection failed.", exception);
        } catch (JedisException exception) {
            throw new RedisQueryException("Redis operation failed: " + exception.getMessage(), exception);
        }
    }
}
