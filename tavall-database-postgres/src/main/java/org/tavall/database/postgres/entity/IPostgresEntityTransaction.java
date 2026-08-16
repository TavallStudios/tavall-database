package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Entity operations scoped to one Tavall Database-owned PostgreSQL
 * transaction.
 *
 * <p>This contract intentionally mirrors the entity store operations that are
 * safe to compose atomically without exposing EntityManager or transaction
 * lifecycle control to application modules.</p>
 */
public interface IPostgresEntityTransaction {

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
}
