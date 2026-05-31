package org.tavall.database.core.query;

import java.util.List;
import java.util.Optional;

public interface IDatabaseQueryHandler {

    boolean executePreparedStatement(String sql, Object... params);

    int executePreparedStatementAndCount(String sql, Object... params);

    <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params);

    <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params);
}
