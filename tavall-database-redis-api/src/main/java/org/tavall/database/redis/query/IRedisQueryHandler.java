package org.tavall.database.redis.query;

import java.time.Duration;
import java.util.Optional;
import org.tavall.database.core.query.IDatabaseQueryHandler;
import org.tavall.database.redis.key.RedisKey;

/**
 * Typed Redis string operations.
 *
 * <p>The inherited SQL-shaped {@link IDatabaseQueryHandler} methods remain only for {@code IDatabase}
 * compatibility; Redis providers report them as unsupported. New consumers use the typed methods below.</p>
 */
public interface IRedisQueryHandler extends IDatabaseQueryHandler {

    Optional<String> get(RedisKey key);

    /** Stores a value without expiry, replacing any existing value and expiry. */
    void set(RedisKey key, String value);

    /** Stores a value that expires after {@code timeToLive}; the duration must be positive. */
    void set(RedisKey key, String value, Duration timeToLive);

    /** Stores the value only when the key is absent. Returns {@code true} when this call created it. */
    boolean setIfAbsent(RedisKey key, String value, Duration timeToLive);

    /** Returns {@code true} when a key was removed. */
    boolean delete(RedisKey key);

    /** Applies a positive expiry to an existing key. Returns {@code false} when the key is absent. */
    boolean expire(RedisKey key, Duration timeToLive);

    /** Remaining expiry; empty when the key is absent or has no expiry. */
    Optional<Duration> timeToLive(RedisKey key);
}
