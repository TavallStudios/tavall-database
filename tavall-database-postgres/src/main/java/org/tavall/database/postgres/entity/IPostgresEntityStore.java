package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides entity-oriented PostgreSQL persistence without exposing transaction
 * callbacks or EntityManager lifecycle ownership to application modules.
 *
 * <p>Domain-specific queries belong on mapped entities as named queries.
 * Tavall Database owns the EntityManager, transaction, flush, rollback, and
 * closure mechanics used to execute these operations.</p>
 */
public interface IPostgresEntityStore {

    boolean isOpen();

    <E, I> Optional<E> find(Class<E> entityType, I id);

    <E, I> Optional<E> find(
            Class<E> entityType,
            I id,
            LockModeType lockModeType
    );

    <E> E save(E entity);

    <E> List<E> saveAll(Collection<E> entities);

    <E> boolean delete(E entity);

    <E, I> boolean deleteById(Class<E> entityType, I id);

    <E> List<E> findNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters,
            int maxResults
    );

    <E> Optional<E> findOneNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters
    );

    int executeNamedMutation(
            String queryName,
            Map<String, ?> parameters
    );

    /**
     * Executes a composed entity operation inside one Tavall Database-owned
     * write transaction.
     *
     * <p>The supplied operation receives typed entity capabilities only. It
     * cannot access or control the underlying EntityManager or transaction.</p>
     */
    <ResultType> ResultType executeAtomic(
            IPostgresEntityAtomicOperation<ResultType> operation
    );
}
