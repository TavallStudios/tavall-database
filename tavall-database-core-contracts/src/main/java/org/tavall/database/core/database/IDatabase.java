package org.tavall.database.core.database;

import org.tavall.database.core.query.IDatabaseQueryHandler;

/**
 * Common lifecycle and access contract implemented by Tavall database backends.
 *
 * <p>A database exposes the backend type and immutable/configuration view used to create it, a
 * backend-appropriate query handler, an availability probe, and explicit resource closure. Backend
 * implementations may support richer APIs in subtype-specific contracts while remaining usable
 * through this minimal common surface.</p>
 */
public interface IDatabase extends AutoCloseable {

    /**
     * Returns the backend type descriptor that defines this database implementation.
     *
     * @return database type descriptor
     */
    IDatabaseType<?, ?> getDatabaseType();

    /**
     * Returns the configuration used by this database instance.
     *
     * @return backend configuration data
     */
    IDatabaseConfigData getConfigData();

    /**
     * Returns the low-level query facade associated with this database.
     *
     * <p>Capabilities vary by backend. Unsupported generic query operations must fail through the
     * query handler's documented sentinel/empty-result semantics rather than being silently
     * interpreted as successful work.</p>
     *
     * @return query handler owned by this database
     */
    IDatabaseQueryHandler queries();

    /**
     * Reports whether the backend is currently usable for new operations.
     *
     * @return {@code true} when the database considers its underlying backend available
     */
    boolean isAvailable();

    /**
     * Releases backend-owned resources and prevents further use where the implementation owns a
     * closeable client, connection pool, or persistence context.
     */
    @Override
    void close();
}
