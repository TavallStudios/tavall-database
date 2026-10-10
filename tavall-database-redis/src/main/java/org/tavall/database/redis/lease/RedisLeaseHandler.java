package org.tavall.database.redis.lease;

import org.tavall.database.redis.RedisProviderCalls;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;
import org.tavall.database.redis.exception.RedisQueryException;
import org.tavall.database.redis.key.RedisKey;

/**
 * Jedis lease implementation. The stored value is {@code token|owner}; renew and release run as Lua scripts
 * that compare the full stored value, so only the exact acquisition can mutate the lease.
 */
public final class RedisLeaseHandler implements IRedisLeaseHandler {

    private static final String ACQUIRE_SCRIPT = """
            if redis.call('SET', KEYS[1], ARGV[1], 'NX', 'PX', ARGV[2]) then
                return redis.call('INCR', KEYS[2])
            end
            return 0
            """;
    private static final String RENEW_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('PEXPIRE', KEYS[1], ARGV[2])
            end
            return 0
            """;
    private static final String RELEASE_SCRIPT = """
            if redis.call('GET', KEYS[1]) == ARGV[1] then
                return redis.call('DEL', KEYS[1])
            end
            return 0
            """;

    private final IJedisRedisConnectionHandler connectionHandler;

    public RedisLeaseHandler(IJedisRedisConnectionHandler connectionHandler) {
        this.connectionHandler = Objects.requireNonNull(connectionHandler, "connectionHandler");
    }

    @Override
    public Optional<RedisLease> acquire(RedisKey key, String owner, Duration timeToLive) {
        RedisLease candidate = new RedisLease(key, owner, UUID.randomUUID().toString(), 1, timeToLive);
        Object reply = RedisProviderCalls.call(() -> connectionHandler.requireClient().eval(ACQUIRE_SCRIPT,
                List.of(key.value(), RedisLeaseEncoding.fencingKey(key)),
                List.of(RedisLeaseEncoding.storedValue(candidate), Long.toString(timeToLive.toMillis()))));
        if (!(reply instanceof Long fencingToken)) {
            throw new RedisQueryException("Redis returned an unexpected lease acquisition reply: " + reply);
        }
        return fencingToken > 0
                ? Optional.of(new RedisLease(key, owner, candidate.token(), fencingToken, timeToLive))
                : Optional.empty();
    }

    @Override
    public RedisLeaseState renew(RedisLease lease, Duration timeToLive) {
        Objects.requireNonNull(lease, "lease");
        long millis = new RedisLease(lease.key(), lease.owner(), lease.token(), lease.fencingToken(), timeToLive)
                .timeToLive().toMillis();
        Object reply = RedisProviderCalls.call(() -> connectionHandler.requireClient().eval(RENEW_SCRIPT,
                List.of(lease.key().value()), List.of(RedisLeaseEncoding.storedValue(lease), Long.toString(millis))));
        return Long.valueOf(1L).equals(reply) ? RedisLeaseState.HELD : RedisLeaseState.LOST;
    }

    @Override
    public RedisLeaseState release(RedisLease lease) {
        Objects.requireNonNull(lease, "lease");
        Object reply = RedisProviderCalls.call(() -> connectionHandler.requireClient().eval(RELEASE_SCRIPT,
                List.of(lease.key().value()), List.of(RedisLeaseEncoding.storedValue(lease))));
        return Long.valueOf(1L).equals(reply) ? RedisLeaseState.HELD : RedisLeaseState.LOST;
    }

    @Override
    public Optional<String> holder(RedisKey key) {
        Objects.requireNonNull(key, "key");
        String stored = RedisProviderCalls.call(() -> connectionHandler.requireClient().get(key.value()));
        if (stored == null) {
            return Optional.empty();
        }
        String owner = RedisLeaseEncoding.owner(stored);
        return owner.isEmpty() ? Optional.empty() : Optional.of(owner);
    }


}
