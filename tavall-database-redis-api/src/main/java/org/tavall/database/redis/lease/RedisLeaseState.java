package org.tavall.database.redis.lease;

/** Outcome of renewing or releasing a lease. */
public enum RedisLeaseState {
    /** The caller still held the lease and the operation was applied. */
    HELD,
    /** The lease expired or is held by another acquisition; nothing was changed. */
    LOST
}
