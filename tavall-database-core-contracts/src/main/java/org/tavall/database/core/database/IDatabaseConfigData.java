package org.tavall.database.core.database;

/**
 * Common configuration view shared by Tavall database backends.
 *
 * <p>Backend-specific configuration interfaces extend this contract with connection or storage
 * details while preserving a stable way to identify the database type, configuration shape, and
 * write policy.</p>
 */
public interface IDatabaseConfigData {

    /**
     * Returns the backend type this configuration is intended to build.
     *
     * @return database type descriptor
     */
    IDatabaseType<?, ?> getDatabaseType();

    /**
     * Returns the structural configuration category used by this backend.
     *
     * @return configuration category such as host/port, JDBC, file, or memory
     */
    DatabaseConfigType getConfigType();

    /**
     * Reports whether mutating operations must be rejected for databases built from this
     * configuration.
     *
     * @return {@code true} when the configured database is read-only
     */
    boolean isReadOnly();
}
