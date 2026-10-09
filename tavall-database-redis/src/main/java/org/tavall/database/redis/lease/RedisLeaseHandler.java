package org.tavall.database.redis.lease;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;
import org.tavall.database.redis.key.RedisKey;
import redis.clients.jedis.params.SetParams;

/**
 * Jedis lease implementation. The stored value is {@code token|owner}; renew and release run as Lua scripts
 * that compare the full stored value, so only the exact acquisition can mutate the lease.
 */
public final class RedisLeaseHandler implements IRedisLeaseHandler {

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
    private static final char SEPARATOR = '|';

    private final IJedisRedisConnectionHandler connectionHandler;

    public RedisLeaseHandler(IJedisRedisConnectionHandler connectionHandler) {
        this.connectionHandler = Objects.requireNonNull(connectionHandler, "connectionHandler");
    }

    @Override
    public Optional<RedisLease> acquire(RedisKey key, String owner, Duration timeToLive) {
        RedisLease candidate = new RedisLease(key, owner, UUID.randomUUID().toString(), timeToLive);
        String reply = connectionHandler.requireClient().set(key.value(), storedValue(candidate),
                SetParams.setParams().nx().px(timeToLive.toMillis()));
        return "OK".equals(reply) ? Optional.of(candidate) : Optional.empty();
    }

    @Override
    public RedisLeaseState renew(RedisLease lease, Duration timeToLive) {
        Objects.requireNonNull(lease, "lease");
        long millis = new RedisLease(lease.key(), lease.owner(), lease.token(), timeToLive).timeToLive().toMillis();
        Object reply = connectionHandler.requireClient().eval(RENEW_SCRIPT, List.of(lease.key().value()),
                List.of(storedValue(lease), Long.toString(millis)));
        return Long.valueOf(1L).equals(reply) ? RedisLeaseState.HELD : RedisLeaseState.LOST;
    }

    @Override
    public RedisLeaseState release(RedisLease lease) {
        Objects.requireNonNull(lease, "lease");
        Object reply = connectionHandler.requireClient().eval(RELEASE_SCRIPT, List.of(lease.key().value()),
                List.of(storedValue(lease)));
        return Long.valueOf(1L).equals(reply) ? RedisLeaseState.HELD : RedisLeaseState.LOST;
    }

    @Override
    public Optional<String> holder(RedisKey key) {
        Objects.requireNonNull(key, "key");
        String stored = connectionHandler.requireClient().get(key.value());
        if (stored == null) {
            return Optional.empty();
        }
        int separator = stored.indexOf(SEPARATOR);
        return separator < 0 ? Optional.empty() : Optional.of(stored.substring(separator + 1));
    }

    private static String storedValue(RedisLease lease) {
        return lease.token() + SEPARATOR + lease.owner();
    }
}
