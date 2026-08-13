package org.tavall.database.core.builder;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseBuilder;

/**
 * Fluent builder contract for databases that expose Tavall-managed Jakarta Persistence support.
 *
 * <p>These options configure Tavall's JPA bootstrap rather than transferring entity-manager or
 * transaction ownership to application code.</p>
 *
 * @param <D> database type produced by the builder
 * @param <B> concrete fluent builder type
 */
public interface IJpaDatabaseBuilder<D extends IDatabase, B extends IJpaDatabaseBuilder<D, B>>
        extends IDatabaseBuilder<D> {

    /**
     * Configures the persistence-unit name used during JPA bootstrap.
     *
     * @param persistenceUnitName persistence-unit identifier
     * @return this fluent builder
     */
    B persistenceUnitName(String persistenceUnitName);

    /**
     * Adds or selects an application package whose mapped entity types should be considered during
     * persistence bootstrap.
     *
     * @param entityPackage package name containing mapped entity classes
     * @return this fluent builder
     */
    B entityPackage(String entityPackage);

    /**
     * Configures whether provider bootstrap may generate/update database schema from mapped types.
     *
     * @param generateSchema {@code true} to enable schema generation behavior
     * @return this fluent builder
     */
    B generateSchema(boolean generateSchema);

    /**
     * Configures provider SQL logging for diagnostic use.
     *
     * @param showSql {@code true} to request SQL output from the configured persistence provider
     * @return this fluent builder
     */
    B showSql(boolean showSql);
}
