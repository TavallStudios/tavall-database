package org.tavall.database.redis.lease;

import java.time.Duration;
import java.util.Objects;
import org.tavall.database.redis.key.RedisKey;

/**
 * Proof of one lease acquisition.
 *
 * @param token unique per acquisition, so a holder whose lease expired and was re-acquired by anyone
 *              (including the same owner) can no longer renew or release it
 * @param fencingToken strictly increasing per lease key across acquisitions; use it as the
 *                     {@code fenceEpoch} of fenced record writes so a stale holder's writes are rejected
 */
public record RedisLease(RedisKey key, String owner, String token, long fencingToken, Duration timeToLive) {

    public RedisLease {
        Objects.requireNonNull(key, "key");
        owner = requireText(owner, "owner");
        token = requireText(token, "token");
        if (fencingToken <= 0) {
            throw new IllegalArgumentException("fencingToken must be positive");
        }
        Objects.requireNonNull(timeToLive, "timeToLive");
        if (timeToLive.toMillis() < 1) {
            throw new IllegalArgumentException("timeToLive must be at least one millisecond");
        }
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank() || value.length() > 256) {
            throw new IllegalArgumentException(field + " must be non-blank and at most 256 characters");
        }
        return value;
    }
}
