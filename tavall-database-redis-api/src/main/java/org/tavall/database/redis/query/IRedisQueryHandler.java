package org.tavall.database.redis.query;

import java.time.Duration;
import java.util.Optional;
import org.tavall.database.core.query.IDatabaseQueryHandler;
import org.tavall.database.redis.key.RedisKey;

/**
 * Typed Redis string operations.
 *
 * <p>Expiry durations must be at least one millisecond; Redis expiry has millisecond resolution. Keys owned by
 * {@code IRedisLeaseHandler} or {@code IRedisVersionedRecordHandler} must not be written through these generic
 * operations: they bypass the lease token and record fence checks. Keep those key families separate.</p>
 *
 * <p>The inherited SQL-shaped {@link IDatabaseQueryHandler} methods remain only for {@code IDatabase}
 * compatibility; Redis providers report them as unsupported. New consumers use the typed methods below.</p>
 */
public interface IRedisQueryHandler extends IDatabaseQueryHandler {

    Optional<String> get(RedisKey key);

    /** Stores a value without expiry, replacing any existing value and expiry. */
    void set(RedisKey key, String value);

    /** Stores a value that expires after {@code timeToLive} (at least one millisecond). */
    void set(RedisKey key, String value, Duration timeToLive);

    /** Stores the value only when the key is absent. Returns {@code true} when this call created it. */
    boolean setIfAbsent(RedisKey key, String value, Duration timeToLive);

    /** Returns {@code true} when a key was removed. */
    boolean delete(RedisKey key);

    /** Applies an expiry of at least one millisecond to an existing key. Returns {@code false} when absent. */
    boolean expire(RedisKey key, Duration timeToLive);

    /** Remaining expiry; empty when the key is absent or has no expiry. */
    Optional<Duration> timeToLive(RedisKey key);
}
