package org.tavall.database.redis.query;

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
        return Optional.ofNullable(client().get(key.value()));
    }

    @Override
    public void set(RedisKey key, String value) {
        Objects.requireNonNull(key, "key");
        client().set(key.value(), Objects.requireNonNull(value, "value"));
    }

    @Override
    public void set(RedisKey key, String value, Duration timeToLive) {
        Objects.requireNonNull(key, "key");
        client().set(key.value(), Objects.requireNonNull(value, "value"), SetParams.setParams().px(positiveMillis(timeToLive)));
    }

    @Override
    public boolean setIfAbsent(RedisKey key, String value, Duration timeToLive) {
        Objects.requireNonNull(key, "key");
        String reply = client().set(key.value(), Objects.requireNonNull(value, "value"),
                SetParams.setParams().nx().px(positiveMillis(timeToLive)));
        return "OK".equals(reply);
    }

    @Override
    public boolean delete(RedisKey key) {
        Objects.requireNonNull(key, "key");
        return client().del(key.value()) > 0;
    }

    @Override
    public boolean expire(RedisKey key, Duration timeToLive) {
        Objects.requireNonNull(key, "key");
        return client().pexpire(key.value(), positiveMillis(timeToLive)) == 1L;
    }

    @Override
    public Optional<Duration> timeToLive(RedisKey key) {
        Objects.requireNonNull(key, "key");
        long remaining = client().pttl(key.value());
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
