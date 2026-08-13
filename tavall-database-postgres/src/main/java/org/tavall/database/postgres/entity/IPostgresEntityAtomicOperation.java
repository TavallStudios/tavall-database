package org.tavall.database.postgres.entity;

/**
 * Defines one application-level entity mutation that Tavall Database executes
 * inside a single owned PostgreSQL/JPA transaction.
 *
 * <p>The operation receives only the typed entity context. It cannot access
 * the underlying EntityManager or control transaction lifecycle.</p>
 */
@FunctionalInterface
public interface IPostgresEntityAtomicOperation<ResultType> {

    ResultType execute(IPostgresEntityOperationContext entities);
}
