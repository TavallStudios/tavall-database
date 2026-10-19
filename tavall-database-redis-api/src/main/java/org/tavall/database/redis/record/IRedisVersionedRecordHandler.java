package org.tavall.database.redis.record;

import java.util.Optional;
import org.tavall.database.redis.key.RedisKey;
import org.tavall.database.redis.lease.RedisLease;

/**
 * Atomic fenced records: create-if-absent and compare-and-set against an observed version and fence epoch.
 * Each operation executes as one server-side script, so concurrent writers serialize and stale writers are
 * rejected without partially applying.
 *
 * <p>{@link #create} and {@link #compareAndSet} compare only the stored version and epoch; they do not consult a
 * lease. Writers coordinated by {@code IRedisLeaseHandler} use {@link #createUnderLease} and
 * {@link #compareAndSetUnderLease}, which verify lease ownership and the fencing token in the same script.</p>
 *
 * <p>Every result's {@code current} record is read inside the same atomic script as the write, so it is
 * exactly the state the operation left or found. To fence writers, set {@code fenceEpoch} from a strictly
 * increasing authority token such as {@code RedisLease.fencingToken()}; only the lease-fenced methods enforce
 * that token against the live lease on the server.</p>
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

    /**
     * Lease-fenced create: stored only when absent and while {@code lease} (this exact acquisition) still holds
     * its key; the record's fence epoch is {@code lease.fencingToken()}. A lost lease yields {@code STALE}.
     */
    RedisRecordWriteResult createUnderLease(RedisKey key, long version, String payload, RedisLease lease);

    /**
     * Lease-fenced compare-and-set, checked entirely inside Redis: applies only when {@code lease} still holds
     * its key, the stored record equals {@code expected}, and the stored epoch is not newer than
     * {@code lease.fencingToken()}. The new record's epoch is the lease's fencing token, so a holder whose lease
     * expired can never overwrite the record, before or after the next holder writes.
     */
    RedisRecordWriteResult compareAndSetUnderLease(RedisKey key, RedisRecordFence expected, long nextVersion,
                                                   String payload, RedisLease lease);

    /** Deletes the record when its stored fence equals {@code expected}. */
    RedisRecordWriteResult delete(RedisKey key, RedisRecordFence expected);
}
