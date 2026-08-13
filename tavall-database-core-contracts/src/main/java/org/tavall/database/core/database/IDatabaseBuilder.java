package org.tavall.database.core.database;

import java.util.Optional;

/**
 * Builds a configured Tavall database instance.
 *
 * <p>Builders use {@link Optional} to represent construction that could not produce a usable
 * database without forcing every backend to expose the same checked exception model. Backend
 * builders should validate their required configuration before attempting connection/bootstrap work
 * and must not return a present database that is already known to be unusable.</p>
 *
 * @param <D> database type produced by the builder
 */
public interface IDatabaseBuilder<D extends IDatabase> {

    /**
     * Attempts to construct the configured database.
     *
     * @return the constructed database when configuration and backend initialization succeed;
     *         otherwise an empty result
     */
    Optional<D> build();
}
