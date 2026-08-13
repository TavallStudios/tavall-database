package org.tavall.database.core.query;

import java.util.List;
import java.util.Optional;

/**
 * Lowest-common-denominator query contract shared by Tavall database backends.
 *
 * <p>The SQL-shaped method signatures exist so relational backends can expose prepared-statement
 * access through the common database API. Non-relational backends that do not support these
 * operations return the same failure sentinels used for unsuccessful execution: {@code false},
 * {@code -1}, an empty {@link Optional}, or an empty list. Callers that require backend-native
 * operations should depend on the backend-specific query interface instead.</p>
 */
public interface IDatabaseQueryHandler {

    /**
     * Executes an update-style prepared statement and reports whether execution completed.
     *
     * @param sql statement text understood by the backend implementation
     * @param params positional parameters bound in declaration order
     * @return {@code true} when execution completed with a non-negative update count;
     *         {@code false} when execution is unsupported or fails
     */
    boolean executePreparedStatement(String sql, Object... params);

    /**
     * Executes an update-style prepared statement and returns the affected-row count.
     *
     * @param sql statement text understood by the backend implementation
     * @param params positional parameters bound in declaration order
     * @return non-negative update count on success, or {@code -1} when execution is unsupported or
     *         fails
     */
    int executePreparedStatementAndCount(String sql, Object... params);

    /**
     * Executes a result-producing query and maps only its first row.
     *
     * @param sql query text understood by the backend implementation
     * @param resultMapper mapper that converts the current result row into the requested value
     * @param params positional parameters bound in declaration order
     * @param <T> mapped result type
     * @return first mapped value, or empty when no row exists, mapping produces {@code null}, the
     *         operation is unsupported, or execution fails
     */
    <T> Optional<T> queryOne(String sql, IDatabaseResultMapper<T> resultMapper, Object... params);

    /**
     * Executes a result-producing query and maps every returned row in backend iteration order.
     *
     * @param sql query text understood by the backend implementation
     * @param resultMapper mapper applied to each result row
     * @param params positional parameters bound in declaration order
     * @param <T> mapped result type
     * @return mapped results, or an empty list when no rows exist, the operation is unsupported, or
     *         execution fails
     */
    <T> List<T> queryList(String sql, IDatabaseResultMapper<T> resultMapper, Object... params);
}
