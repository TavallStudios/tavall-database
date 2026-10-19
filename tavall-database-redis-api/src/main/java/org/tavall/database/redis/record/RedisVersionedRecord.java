package org.tavall.database.redis.record;

import java.util.Objects;

/**
 * Versioned payload stored with its fence epoch.
 *
 * @param version monotonically increasing record version, starting at 1
 * @param fenceEpoch writer authority epoch (for example a lease generation); never decreases
 * @param payload domain-owned serialized payload; its schema belongs to the domain owner
 */
public record RedisVersionedRecord(long version, long fenceEpoch, String payload) {

    public RedisVersionedRecord {
        if (version <= 0) {
            throw new IllegalArgumentException("version must be positive");
        }
        if (fenceEpoch <= 0) {
            throw new IllegalArgumentException("fenceEpoch must be positive");
        }
        Objects.requireNonNull(payload, "payload");
    }

    public RedisRecordFence fence() {
        return new RedisRecordFence(version, fenceEpoch);
    }
}
