package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides entity-oriented PostgreSQL persistence without exposing transaction callbacks or
 * {@code EntityManager} lifecycle ownership to application modules.
 *
 * <p>Domain-specific queries belong on mapped entities as named queries. Tavall Database owns the
 * entity manager, transaction, flush, rollback, and closure mechanics used to execute these
 * operations. Mutating methods execute through the JPA write boundary; ordinary unlocked reads use
 * the read boundary. Reads that request a lock mode other than {@link LockModeType#NONE} use the
 * write boundary so the lock participates in a resource-local transaction.</p>
 */
public interface IPostgresEntityStore {

    /**
     * Reports whether the underlying JPA context currently accepts new operations.
     *
     * @return {@code true} when the store's persistence context is open
     */
    boolean isOpen();

    /**
     * Finds an entity by primary key without requesting a persistence lock.
     *
     * @param entityType mapped entity type
     * @param id primary-key value
     * @param <E> entity type
     * @param <I> identifier type
     * @return matching entity, or empty when no row exists
     */
    <E, I> Optional<E> find(Class<E> entityType, I id);

    /**
     * Finds an entity by primary key using an optional JPA lock mode.
     *
     * <p>A null lock mode is treated as {@link LockModeType#NONE}. Non-NONE modes execute inside
     * the store's write transaction boundary.</p>
     *
     * @param entityType mapped entity type
     * @param id primary-key value
     * @param lockModeType requested JPA lock mode
     * @param <E> entity type
     * @param <I> identifier type
     * @return matching entity, or empty when no row exists
     * @throws NullPointerException if {@code entityType} or {@code id} is {@code null}
     */
    <E, I> Optional<E> find(
            Class<E> entityType,
            I id,
            LockModeType lockModeType
    );

    /**
     * Persists the state of an entity through JPA merge semantics.
     *
     * <p>The returned value is the managed instance produced by {@code EntityManager.merge}; it may
     * be a different object from the supplied entity.</p>
     *
     * @param entity entity state to merge
     * @param <E> entity type
     * @return managed merged entity after a successful transaction
     * @throws NullPointerException if {@code entity} is {@code null}
     */
    <E> E save(E entity);

    /**
     * Merges a collection of entities inside one transaction and returns the managed results in the
     * same iteration order as the supplied collection.
     *
     * @param entities entities to merge
     * @param <E> entity type
     * @return immutable list of managed merged entities
     * @throws NullPointerException if {@code entities} is {@code null}
     * @throws IllegalArgumentException if the collection contains {@code null}
     */
    <E> List<E> saveAll(Collection<E> entities);

    /**
     * Removes an entity inside a write transaction.
     *
     * <p>Detached entities are merged first so removal operates on a managed instance.</p>
     *
     * @param entity entity to remove
     * @param <E> entity type
     * @return {@code true} after the remove operation is scheduled successfully
     * @throws NullPointerException if {@code entity} is {@code null}
     */
    <E> boolean delete(E entity);

    /**
     * Removes an entity by primary key using a pessimistic write lock.
     *
     * @param entityType mapped entity type
     * @param id primary-key value
     * @param <E> entity type
     * @param <I> identifier type
     * @return {@code true} when an entity existed and was removed; {@code false} when no row matched
     * @throws NullPointerException if {@code entityType} or {@code id} is {@code null}
     */
    <E, I> boolean deleteById(Class<E> entityType, I id);

    /**
     * Executes a typed JPA named query through the read boundary.
     *
     * <p>Parameter maps may be null or empty. Non-positive {@code maxResults} means no explicit
     * result limit.</p>
     *
     * @param entityType result entity type declared by the named query
     * @param queryName non-blank named-query identifier
     * @param parameters named parameter values, or {@code null} for none
     * @param maxResults maximum rows to return; non-positive values disable the explicit limit
     * @param <E> entity result type
     * @return immutable result list in query order
     */
    <E> List<E> findNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters,
            int maxResults
    );

    /**
     * Executes a typed named query limited to its first result.
     *
     * @param entityType result entity type declared by the named query
     * @param queryName non-blank named-query identifier
     * @param parameters named parameter values, or {@code null} for none
     * @param <E> entity result type
     * @return first result, or empty when the query returns no rows
     */
    <E> Optional<E> findOneNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters
    );

    /**
     * Executes a named update/delete mutation inside the write transaction boundary.
     *
     * @param queryName non-blank named mutation identifier
     * @param parameters named parameter values, or {@code null} for none
     * @return provider-reported number of affected rows
     */
    int executeNamedMutation(
            String queryName,
            Map<String, ?> parameters
    );
}
