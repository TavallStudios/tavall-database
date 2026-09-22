package org.tavall.database.postgres.query;

import org.tavall.database.core.query.IDatabaseQueryHandler;

public interface IPostgresQueryHandler extends IDatabaseQueryHandler {

    /**
     * Executes one trusted SQL statement without JDBC parameter binding.
     *
     * <p>This boundary is for schema and database-owned statements that use
     * SQL syntax such as PostgreSQL dollar-quoted function bodies. Callers
     * must not interpolate untrusted values into the statement.</p>
     */
    boolean executeStatement(String sql);
}
