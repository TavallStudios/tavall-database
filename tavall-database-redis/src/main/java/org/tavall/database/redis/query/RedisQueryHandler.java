package org.tavall.database.redis.query;

import org.tavall.database.core.query.IDatabaseResultMapper;
import org.tavall.database.redis.connection.IRedisConnectionHandler;
import org.tavall.database.redis.exception.RedisQueryException;
import org.tavall.logging.Log;

import java.util.List;
import java.util.Optional;

public final class RedisQueryHandler implements IRedisQueryHandler {

    private final IRedisConnectionHandler connectionHandler;

    public RedisQueryHandler(IRedisConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public boolean executePreparedStatement(String sql, Object... params) {
        return executePreparedStatementAndCount(sql, params) >= 0;
    }

    @Override
    public int executePreparedStatementAndCount(String sql, Object... params) {
        logUnsupported("Redis prepared statements are not supported by this module yet.");
        return -1;
    }

    @Override
    public <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Redis query-one operations are not supported by this module yet.");
        return Optional.empty();
    }

    @Override
    public <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Redis query-list operations are not supported by this module yet.");
        return List.of();
    }

    private void logUnsupported(String message) {
        RedisQueryException exception = new RedisQueryException(message);
        Log.exception(exception);
    }
}
