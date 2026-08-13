package org.tavall.database.core.database;

/**
 * Describes one Tavall database backend and creates builders for that backend.
 *
 * <p>The type identifier is the stable programmatic identity of the backend. The builder type is
 * coupled to the database type through generics so callers can retain backend-specific builder
 * capabilities without unsafe casts.</p>
 *
 * @param <D> database implementation represented by this type
 * @param <B> builder that creates that database implementation
 */
public interface IDatabaseType<D extends IDatabase, B extends IDatabaseBuilder<D>> {

    /**
     * Returns the stable identifier used to distinguish this backend type.
     *
     * @return backend type identifier
     */
    String getTypeId();

    /**
     * Creates a new mutable builder for this backend.
     *
     * @return fresh backend-specific database builder
     */
    B createBuilder();
}
