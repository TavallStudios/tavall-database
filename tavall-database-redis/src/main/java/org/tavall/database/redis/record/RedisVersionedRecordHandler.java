package org.tavall.database.redis.record;

import org.tavall.database.redis.RedisProviderCalls;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;
import org.tavall.database.redis.exception.RedisQueryException;
import org.tavall.database.redis.key.RedisKey;

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
    public RedisRecordWriteResult delete(RedisKey key, RedisRecordFence expected) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(expected, "expected");
        return eval(DELETE_SCRIPT, key, List.of(
                Long.toString(expected.version()), Long.toString(expected.fenceEpoch())));
    }

    private RedisRecordWriteResult eval(String script, RedisKey key, List<String> arguments) {
        Object reply = RedisProviderCalls.call(() -> connectionHandler.requireClient().eval(script, List.of(key.value()), arguments));
        if (!(reply instanceof List<?> fields) || fields.size() != 4) {
            throw new RedisQueryException("Redis returned an unexpected fenced-record reply: " + reply);
        }
        RedisRecordWriteState state = RedisRecordWriteState.valueOf(String.valueOf(fields.get(0)));
        String version = String.valueOf(fields.get(1));
        if (version.isEmpty()) {
            return new RedisRecordWriteResult(state, Optional.empty());
        }
        try {
            return new RedisRecordWriteResult(state, Optional.of(new RedisVersionedRecord(Long.parseLong(version),
                    Long.parseLong(String.valueOf(fields.get(2))), String.valueOf(fields.get(3)))));
        } catch (RuntimeException exception) {
            throw new RedisQueryException("Redis key " + key.value() + " does not hold a versioned record.", exception);
        }
    }
}
