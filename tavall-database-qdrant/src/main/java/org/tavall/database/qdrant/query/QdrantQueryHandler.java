package org.tavall.database.qdrant.query;

import org.tavall.database.core.query.IDatabaseResultMapper;
import org.tavall.database.qdrant.connection.IQdrantConnectionHandler;
import org.tavall.database.qdrant.exception.QdrantQueryException;
import org.tavall.logging.Log;

import java.util.List;
import java.util.Optional;

public final class QdrantQueryHandler implements IQdrantQueryHandler {

    private final IQdrantConnectionHandler connectionHandler;

    public QdrantQueryHandler(IQdrantConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    @Override
    public boolean executePreparedStatement(String sql, Object... params) {
        return executePreparedStatementAndCount(sql, params) >= 0;
    }

    @Override
    public int executePreparedStatementAndCount(String sql, Object... params) {
        logUnsupported("Qdrant prepared statements are not supported by this module yet.");
        return -1;
    }

    @Override
    public <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Qdrant query-one operations are not supported by this module yet.");
        return Optional.empty();
    }

    @Override
    public <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params) {
        logUnsupported("Qdrant query-list operations are not supported by this module yet.");
        return List.of();
    }

    private void logUnsupported(String message) {
        QdrantQueryException exception = new QdrantQueryException(message);
        Log.exception(exception);
    }
}
