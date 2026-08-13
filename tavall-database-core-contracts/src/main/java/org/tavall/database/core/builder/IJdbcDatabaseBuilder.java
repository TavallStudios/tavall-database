package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

/**
 * Fluent builder contract for databases configured through a JDBC connection URL.
 *
 * <p>Configuration methods record builder state and return the same logical builder type so
 * backend-specific builders can extend the fluent API without casts. Required-value validation may
 * occur when values are assigned or when {@link #build()} is attempted, depending on the backend.</p>
 *
 * @param <D> database type produced by the builder
 * @param <B> concrete fluent builder type
 */
public interface IJdbcDatabaseBuilder<D extends IDatabase, B extends IJdbcDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    /**
     * Configures the JDBC URL used to connect to the database.
     *
     * @param jdbcUrl JDBC connection URL
     * @return this fluent builder
     */
    B jdbcUrl(String jdbcUrl);

    /**
     * Configures the database username.
     *
     * @param username database account name
     * @return this fluent builder
     */
    B username(String username);

    /**
     * Configures the database password.
     *
     * @param password database account password
     * @return this fluent builder
     */
    B password(String password);

    /**
     * Configures whether databases produced by this builder must reject mutating operations.
     *
     * @param readOnly {@code true} to build a read-only database
     * @return this fluent builder
     */
    B readOnly(boolean readOnly);
}
