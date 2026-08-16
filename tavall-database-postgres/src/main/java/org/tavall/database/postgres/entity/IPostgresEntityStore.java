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
     * Executes one typed entity operation inside a single transaction owned by
     * Tavall Database.
     *
     * <p>Use this boundary when one application mutation must compose entity
     * reads, locks, writes, and audit/idempotency records atomically. The
     * application receives an entity-oriented transaction contract rather
     * than EntityManager or transaction lifecycle access.</p>
     */
    <R> R execute(IPostgresEntityOperation<R> operation);
}
