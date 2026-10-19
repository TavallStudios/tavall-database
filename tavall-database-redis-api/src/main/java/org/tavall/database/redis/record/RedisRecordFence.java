package org.tavall.database.redis.record;

/** The exact version and fence epoch a writer observed and expects to replace. */
public record RedisRecordFence(long version, long fenceEpoch) {

    public RedisRecordFence {
        if (version <= 0 || fenceEpoch <= 0) {
            throw new IllegalArgumentException("version and fenceEpoch must be positive");
        }
    }
}
