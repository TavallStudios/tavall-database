package org.tavall.database.redis.key;

import java.util.Objects;

/**
 * Typed Redis key. The key text is owned by the consuming domain; this type only enforces that the key is
 * bounded, non-empty, and free of control characters so typed capabilities never receive raw untrusted text.
 */
public record RedisKey(String value) {

    public static final int MAXIMUM_LENGTH = 1_024;
    private static final String SEPARATOR = ":";

    public RedisKey {
        Objects.requireNonNull(value, "value");
        if (value.isBlank() || value.length() > MAXIMUM_LENGTH) {
            throw new IllegalArgumentException("Redis key must be non-blank and at most " + MAXIMUM_LENGTH + " characters");
        }
        for (int index = 0; index < value.length(); index++) {
            if (Character.isISOControl(value.charAt(index))) {
                throw new IllegalArgumentException("Redis key must not contain control characters");
            }
        }
    }

    /** Joins a domain namespace and its key segments with {@code :}; every segment must be non-blank. */
    public static RedisKey of(String namespace, String... segments) {
        StringBuilder key = new StringBuilder(requireSegment(namespace));
        for (String segment : segments) {
            key.append(SEPARATOR).append(requireSegment(segment));
        }
        return new RedisKey(key.toString());
    }

    private static String requireSegment(String segment) {
        if (segment == null || segment.isBlank()) {
            throw new IllegalArgumentException("Redis key segments must be non-blank");
        }
        return segment;
    }
}
