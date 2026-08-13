package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

/**
 * Fluent builder contract for databases addressed through a host and port.
 *
 * <p>The contract intentionally does not impose provider-specific ranges, authentication
 * requirements, or URI synthesis rules. Implementations own those validations when values are
 * assigned or when {@link #build()} is attempted.</p>
 *
 * @param <D> database type produced by the builder
 * @param <B> concrete fluent builder type
 */
public interface IHostPortDatabaseBuilder<D extends IDatabase, B extends IHostPortDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    /**
     * Configures the database host name or address.
     *
     * @param host backend host
     * @return this fluent builder
     */
    B host(String host);

    /**
     * Configures the backend service port.
     *
     * @param port backend port
     * @return this fluent builder
     */
    B port(int port);

    /**
     * Configures the backend account name when authentication is required.
     *
     * @param username account name
     * @return this fluent builder
     */
    B username(String username);

    /**
     * Configures the backend account password when authentication is required.
     *
     * @param password account password
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
