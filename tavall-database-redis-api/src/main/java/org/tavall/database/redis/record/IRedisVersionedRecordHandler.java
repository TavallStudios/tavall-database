package org.tavall.database.redis.record;

import java.util.Optional;
import org.tavall.database.redis.key.RedisKey;

/**
 * Atomic fenced records: create-if-absent and compare-and-set against an observed version and fence epoch.
 * Each operation executes as one server-side script, so concurrent writers serialize and stale writers are
 * rejected without partially applying.
 *
 * <p>Key layout, payload schema, indexes, and reconciliation policy remain with the domain owner.</p>
 */
public interface IRedisVersionedRecordHandler {

    Optional<RedisVersionedRecord> read(RedisKey key);

    /** Stores {@code initial} only when no record exists ({@link RedisRecordWriteState#APPLIED} or {@code EXISTS}). */
    RedisRecordWriteResult create(RedisKey key, RedisVersionedRecord initial);

    /**
     * Replaces the record when its stored fence equals {@code expected}. {@code next} must advance the version
     * and must not lower the fence epoch; violations are rejected before Redis is contacted.
     */
    RedisRecordWriteResult compareAndSet(RedisKey key, RedisRecordFence expected, RedisVersionedRecord next);

    /** Deletes the record when its stored fence equals {@code expected}. */
    RedisRecordWriteResult delete(RedisKey key, RedisRecordFence expected);
}
