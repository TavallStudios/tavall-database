package org.tavall.database.redis.lease;

import java.time.Duration;
import java.util.Objects;
import org.tavall.database.redis.key.RedisKey;

/**
 * Proof of one lease acquisition. {@code token} is unique per acquisition, so a holder whose lease expired
 * and was re-acquired by anyone (including the same owner) can no longer renew or release it.
 */
public record RedisLease(RedisKey key, String owner, String token, Duration timeToLive) {

    public RedisLease {
        Objects.requireNonNull(key, "key");
        owner = requireText(owner, "owner");
        token = requireText(token, "token");
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
