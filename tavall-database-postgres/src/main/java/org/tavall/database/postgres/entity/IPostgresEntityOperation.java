package org.tavall.database.postgres.entity;

/**
 * A typed application persistence operation executed inside one PostgreSQL
 * entity transaction owned by Tavall Database.
 *
 * <p>The operation receives only the entity-oriented transaction contract.
 * EntityManager and transaction lifecycle access remain internal to Tavall
 * Database.</p>
 *
 * @param <R> operation result type
 */
@FunctionalInterface
public interface IPostgresEntityOperation<R> {

    R execute(IPostgresEntityTransaction transaction);
}
