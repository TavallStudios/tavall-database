package org.tavall.database.redis.record;

import java.util.Objects;
import java.util.Optional;

/**
 * Result of a fenced record write.
 *
 * @param state the write outcome
 * @param current the record stored after the operation; empty when no record exists
 */
public record RedisRecordWriteResult(RedisRecordWriteState state, Optional<RedisVersionedRecord> current) {

    public RedisRecordWriteResult {
        Objects.requireNonNull(state, "state");
        Objects.requireNonNull(current, "current");
    }

    public boolean applied() {
        return state == RedisRecordWriteState.APPLIED;
    }
}
