package org.tavall.database.redis.record;

import org.tavall.database.redis.RedisProviderCalls;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;
import org.tavall.database.redis.exception.RedisQueryException;
import org.tavall.database.redis.key.RedisKey;
import org.tavall.database.redis.lease.RedisLease;
import org.tavall.database.redis.lease.RedisLeaseEncoding;

/**
 * Jedis fenced-record implementation over a hash with {@code version}, {@code fenceEpoch}, and
 * {@code payload} fields. Every write is one Lua script so the fence comparison and the write are atomic.
 */
public final class RedisVersionedRecordHandler implements IRedisVersionedRecordHandler {

    private static final String VERSION = "version";
    private static final String FENCE_EPOCH = "fenceEpoch";
    private static final String PAYLOAD = "payload";

    // Every script returns {state, version, fenceEpoch, payload} read inside the same atomic script, so the
    // reported current record is exactly the state this operation left (or found), never a later writer's.
    private static final String CURRENT = """
            local function current(state)
                local fields = redis.call('HMGET', KEYS[1], 'version', 'fenceEpoch', 'payload')
                return {state, fields[1] or '', fields[2] or '', fields[3] or ''}
            end
            """;
    private static final String CREATE_SCRIPT = CURRENT + """
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return current('EXISTS')
            end
            redis.call('HSET', KEYS[1], 'version', ARGV[1], 'fenceEpoch', ARGV[2], 'payload', ARGV[3])
            return current('APPLIED')
            """;
    private static final String COMPARE_AND_SET_SCRIPT = CURRENT + """
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return current('MISSING')
            end
            if redis.call('HGET', KEYS[1], 'version') ~= ARGV[1]
                    or redis.call('HGET', KEYS[1], 'fenceEpoch') ~= ARGV[2] then
                return current('STALE')
            end
            redis.call('HSET', KEYS[1], 'version', ARGV[3], 'fenceEpoch', ARGV[4], 'payload', ARGV[5])
            return current('APPLIED')
            """;
    private static final String DELETE_SCRIPT = CURRENT + """
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return current('MISSING')
            end
            if redis.call('HGET', KEYS[1], 'version') ~= ARGV[1]
                    or redis.call('HGET', KEYS[1], 'fenceEpoch') ~= ARGV[2] then
                return current('STALE')
            end
            redis.call('DEL', KEYS[1])
            return current('APPLIED')
            """;

    // KEYS: record, lease key. ARGV: lease stored value, lease fencing token, next version, payload.
    private static final String CREATE_UNDER_LEASE_SCRIPT = CURRENT + """
            if redis.call('GET', KEYS[2]) ~= ARGV[1] then
                return current('STALE')
            end
            if redis.call('EXISTS', KEYS[1]) == 1 then
                return current('EXISTS')
            end
            redis.call('HSET', KEYS[1], 'version', ARGV[3], 'fenceEpoch', ARGV[2], 'payload', ARGV[4])
            return current('APPLIED')
            """;
    // KEYS: record, lease key. ARGV: lease stored value, lease fencing token, expected version, expected epoch,
    // next version, payload.
    private static final String COMPARE_AND_SET_UNDER_LEASE_SCRIPT = CURRENT + """
            if redis.call('GET', KEYS[2]) ~= ARGV[1] then
                return current('STALE')
            end
            if redis.call('EXISTS', KEYS[1]) == 0 then
                return current('MISSING')
            end
            local storedEpoch = redis.call('HGET', KEYS[1], 'fenceEpoch')
            if redis.call('HGET', KEYS[1], 'version') ~= ARGV[3] or storedEpoch ~= ARGV[4]
                    or tonumber(storedEpoch) > tonumber(ARGV[2]) then
                return current('STALE')
            end
            redis.call('HSET', KEYS[1], 'version', ARGV[5], 'fenceEpoch', ARGV[2], 'payload', ARGV[6])
            return current('APPLIED')
            """;

    private final IJedisRedisConnectionHandler connectionHandler;

    public RedisVersionedRecordHandler(IJedisRedisConnectionHandler connectionHandler) {
        this.connectionHandler = Objects.requireNonNull(connectionHandler, "connectionHandler");
    }

    @Override
    public Optional<RedisVersionedRecord> read(RedisKey key) {
        Objects.requireNonNull(key, "key");
        Map<String, String> fields = RedisProviderCalls.call(() -> connectionHandler.requireClient().hgetAll(key.value()));
        if (fields == null || fields.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(new RedisVersionedRecord(
                    Long.parseLong(fields.get(VERSION)),
                    Long.parseLong(fields.get(FENCE_EPOCH)),
                    Objects.requireNonNull(fields.get(PAYLOAD), PAYLOAD)
            ));
        } catch (RuntimeException exception) {
            throw new RedisQueryException("Redis key " + key.value() + " does not hold a versioned record.", exception);
        }
    }

    @Override
    public RedisRecordWriteResult create(RedisKey key, RedisVersionedRecord initial) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(initial, "initial");
        return eval(CREATE_SCRIPT, key, List.of(
                Long.toString(initial.version()), Long.toString(initial.fenceEpoch()), initial.payload()));
    }

    @Override
    public RedisRecordWriteResult compareAndSet(RedisKey key, RedisRecordFence expected, RedisVersionedRecord next) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(next, "next");
        if (next.version() <= expected.version()) {
            throw new IllegalArgumentException("next version must advance beyond the expected version");
        }
        if (next.fenceEpoch() < expected.fenceEpoch()) {
            throw new IllegalArgumentException("next fence epoch must not precede the expected fence epoch");
        }
        return eval(COMPARE_AND_SET_SCRIPT, key, List.of(
                Long.toString(expected.version()), Long.toString(expected.fenceEpoch()),
                Long.toString(next.version()), Long.toString(next.fenceEpoch()), next.payload()));
    }

    @Override
    public RedisRecordWriteResult createUnderLease(RedisKey key, long version, String payload, RedisLease lease) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(lease, "lease");
        RedisVersionedRecord initial = new RedisVersionedRecord(version, lease.fencingToken(), payload);
        return eval(CREATE_UNDER_LEASE_SCRIPT, List.of(key.value(), lease.key().value()), List.of(
                RedisLeaseEncoding.storedValue(lease), Long.toString(lease.fencingToken()),
                Long.toString(initial.version()), initial.payload()), key);
    }

    @Override
    public RedisRecordWriteResult compareAndSetUnderLease(RedisKey key, RedisRecordFence expected, long nextVersion,
                                                          String payload, RedisLease lease) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(expected, "expected");
        Objects.requireNonNull(lease, "lease");
        RedisVersionedRecord next = new RedisVersionedRecord(nextVersion, lease.fencingToken(), payload);
        if (next.version() <= expected.version()) {
            throw new IllegalArgumentException("next version must advance beyond the expected version");
        }
        return eval(COMPARE_AND_SET_UNDER_LEASE_SCRIPT, List.of(key.value(), lease.key().value()), List.of(
                RedisLeaseEncoding.storedValue(lease), Long.toString(lease.fencingToken()),
                Long.toString(expected.version()), Long.toString(expected.fenceEpoch()),
                Long.toString(next.version()), next.payload()), key);
    }

    @Override
    public RedisRecordWriteResult delete(RedisKey key, RedisRecordFence expected) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(expected, "expected");
        return eval(DELETE_SCRIPT, key, List.of(
                Long.toString(expected.version()), Long.toString(expected.fenceEpoch())));
    }

    private RedisRecordWriteResult eval(String script, RedisKey key, List<String> arguments) {
        return eval(script, List.of(key.value()), arguments, key);
    }

    private RedisRecordWriteResult eval(String script, List<String> keys, List<String> arguments, RedisKey key) {
        Object reply = RedisProviderCalls.call(() -> connectionHandler.requireClient().eval(script, keys, arguments));
        if (!(reply instanceof List<?> fields) || fields.size() != 4) {
            throw new RedisQueryException("Redis returned an unexpected fenced-record reply: " + reply);
        }
        try {
            RedisRecordWriteState state = RedisRecordWriteState.valueOf(String.valueOf(fields.get(0)));
            String version = String.valueOf(fields.get(1));
            if (version.isEmpty()) {
                if (state != RedisRecordWriteState.MISSING && state != RedisRecordWriteState.STALE
                        && !(state == RedisRecordWriteState.APPLIED && script.equals(DELETE_SCRIPT))) {
                    throw new RedisQueryException("Redis key " + key.value() + " does not hold a versioned record.");
                }
                return new RedisRecordWriteResult(state, Optional.empty());
            }
            return new RedisRecordWriteResult(state, Optional.of(new RedisVersionedRecord(Long.parseLong(version),
                    Long.parseLong(String.valueOf(fields.get(2))), String.valueOf(fields.get(3)))));
        } catch (RedisQueryException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new RedisQueryException("Redis key " + key.value() + " does not hold a versioned record.", exception);
        }
    }
}
