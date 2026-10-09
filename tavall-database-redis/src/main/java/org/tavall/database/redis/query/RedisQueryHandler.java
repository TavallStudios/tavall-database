package org.tavall.database.redis.query;

import org.tavall.database.redis.RedisProviderCalls;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.tavall.database.core.query.IDatabaseResultMapper;
import org.tavall.database.redis.connection.IJedisRedisConnectionHandler;
import org.tavall.database.redis.exception.RedisQueryException;
import org.tavall.database.redis.key.RedisKey;
import org.tavall.logging.Log;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.SetParams;

/** Jedis implementation of typed Redis string operations. */
public final class RedisQueryHandler implements IRedisQueryHandler {

    private final IJedisRedisConnectionHandler connectionHandler;

    public RedisQueryHandler(IJedisRedisConnectionHandler connectionHandler) {
        this.connectionHandler = Objects.requireNonNull(connectionHandler, "connectionHandler");
    }

    @Override
    public Optional<String> get(RedisKey key) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(RedisProviderCalls.call(() -> client().get(key.value())));
    }

    @Override
    public void set(RedisKey key, String value) {
        Objects.requireNonNull(key, "key");
        RedisProviderCalls.call(() -> client().set(key.value(), Objects.requireNonNull(value, "value")));
    }

    @Override
    public void set(RedisKey key, String value, Duration timeToLive) {
        Objects.requireNonNull(key, "key");
        long millis = positiveMillis(timeToLive);
        RedisProviderCalls.call(() -> client().set(key.value(), Objects.requireNonNull(value, "value"),
                SetParams.setParams().px(millis)));
    }

    @Override
    public boolean setIfAbsent(RedisKey key, String value, Duration timeToLive) {
        Objects.requireNonNull(key, "key");
        long millis = positiveMillis(timeToLive);
        String reply = RedisProviderCalls.call(() -> client().set(key.value(), Objects.requireNonNull(value, "value"),
                SetParams.setParams().nx().px(millis)));
        return "OK".equals(reply);
    }

    @Override
    public boolean delete(RedisKey key) {
        Objects.requireNonNull(key, "key");
        return RedisProviderCalls.call(() -> client().del(key.value())) > 0;
    }

    @Override
    public boolean expire(RedisKey key, Duration timeToLive) {
        Objects.requireNonNull(key, "key");
        return RedisProviderCalls.call(() -> client().pexpire(key.value(), positiveMillis(timeToLive))) == 1L;
    }

    @Override
    public Optional<Duration> timeToLive(RedisKey key) {
        Objects.requireNonNull(key, "key");
        long remaining = RedisProviderCalls.call(() -> client().pttl(key.value()));
        return remaining >= 0 ? Optional.of(Duration.ofMillis(remaining)) : Optional.empty();
    }

    @Override
    public boolean executePreparedStatement(String sql, Object... params) {
        return executePreparedStatementAndCount(sql, params) >= 0;
    }

    @Override
    public int executePreparedStatementAndCount(String sql, Object... params) {
        logUnsupported("Redis does not support SQL prepared statements; use the typed Redis operations.");
        return -1;
    }

    @Override
    public <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Redis does not support SQL queries; use the typed Redis operations.");
        return Optional.empty();
    }

    @Override
    public <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Redis does not support SQL queries; use the typed Redis operations.");
        return List.of();
    }

    static long positiveMillis(Duration timeToLive) {
        Objects.requireNonNull(timeToLive, "timeToLive");
        long millis = timeToLive.toMillis();
        if (millis <= 0) {
            throw new IllegalArgumentException("timeToLive must be at least one millisecond");
        }
        return millis;
    }

    private JedisPooled client() {
        return connectionHandler.requireClient();
    }

    private void logUnsupported(String message) {
        RedisQueryException exception = new RedisQueryException(message);
        Log.exception(exception);
    }
}
