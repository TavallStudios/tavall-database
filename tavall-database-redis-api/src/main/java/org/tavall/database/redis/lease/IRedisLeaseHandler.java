package org.tavall.database.redis.lease;

import java.time.Duration;
import java.util.Optional;
import org.tavall.database.redis.key.RedisKey;

/**
 * Expiring mutual-exclusion leases. Acquire is atomic set-if-absent with expiry; renew and release are
 * compare-by-token so a stale holder can never extend or delete another holder's lease.
 *
 * <p>A lease is coordination, not durable authority: the domain owner still fences its durable writes.</p>
 */
public interface IRedisLeaseHandler {

    /** Acquires the lease when free. Empty when another acquisition currently holds it. */
    Optional<RedisLease> acquire(RedisKey key, String owner, Duration timeToLive);

    /** Extends a held lease to {@code timeToLive} from now. */
    RedisLeaseState renew(RedisLease lease, Duration timeToLive);

    /** Releases a held lease. Releasing a lost lease is a no-op reported as {@link RedisLeaseState#LOST}. */
    RedisLeaseState release(RedisLease lease);

    /** Current holder's owner label, when held. */
    Optional<String> holder(RedisKey key);
}
