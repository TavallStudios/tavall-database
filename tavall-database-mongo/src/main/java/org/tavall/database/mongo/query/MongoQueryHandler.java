package org.tavall.database.mongo.query;

import org.tavall.database.core.query.IDatabaseResultMapper;
import org.tavall.database.mongo.connection.IMongoConnectionHandler;
import org.tavall.database.mongo.exception.MongoQueryException;
import org.tavall.logging.Log;

import java.util.List;
import java.util.Optional;

public final class MongoQueryHandler implements IMongoQueryHandler {

    private final IMongoConnectionHandler connectionHandler;

    public MongoQueryHandler(IMongoConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public boolean executePreparedStatement(String sql, Object... params) {
        return executePreparedStatementAndCount(sql, params) >= 0;
    }

    @Override
    public int executePreparedStatementAndCount(String sql, Object... params) {
        logUnsupported("Mongo prepared statements are not supported by this module yet.");
        return -1;
    }

    @Override
    public <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Mongo query-one operations are not supported by this module yet.");
        return Optional.empty();
    }

    @Override
    public <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Mongo query-list operations are not supported by this module yet.");
        return List.of();
    }

    private void logUnsupported(String message) {
        MongoQueryException exception = new MongoQueryException(message);
        Log.exception(exception);
    }
}
