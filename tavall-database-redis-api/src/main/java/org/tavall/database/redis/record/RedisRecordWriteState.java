package org.tavall.database.redis.record;

/** Outcome of a fenced record write. */
public enum RedisRecordWriteState {
    /** The write was applied. */
    APPLIED,
    /** Create found an existing record; the current record is returned unchanged. */
    EXISTS,
    /** The stored version or fence epoch did not match the caller's fence. */
    STALE,
    /** Compare-and-set or delete targeted a record that does not exist. */
    MISSING
}
