package org.tavall.database.redis.lease;

import java.util.Objects;
import org.tavall.database.redis.key.RedisKey;

/**
 * Stored layout of a Jedis lease, shared by the lease and fenced-record scripts: the lease key holds
 * {@code token|owner} and {@code <key>:fencing-token} holds the per-key monotonic counter. The provider is
 * standalone Redis; under Redis Cluster both keys would need one hash slot.
 */
public final class RedisLeaseEncoding {

    private static final char SEPARATOR = '|';

    private RedisLeaseEncoding() {
    }

    public static String storedValue(RedisLease lease) {
        Objects.requireNonNull(lease, "lease");
        return lease.token() + SEPARATOR + lease.owner();
    }

    public static String fencingKey(RedisKey key) {
        return Objects.requireNonNull(key, "key").value() + ":fencing-token";
    }

    /** Owner label from a stored value, or empty text when the value is not a lease. */
    public static String owner(String stored) {
        int separator = stored.indexOf(SEPARATOR);
        return separator < 0 ? "" : stored.substring(separator + 1);
    }
}
