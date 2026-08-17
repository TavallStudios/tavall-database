package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Entity operations available inside one Tavall Database-owned atomic write.
 *
 * <p>This contract intentionally exposes no EntityManager, connection, or
 * transaction lifecycle. Application code may compose typed entity behavior
 * while Tavall Database remains the sole transaction owner.</p>
 *
 * <p>The context is scoped to the thread executing
 * {@link IPostgresEntityStore#executeAtomic(IPostgresEntityAtomicOperation)}.
 * Callers must not retain it after the atomic operation returns or hand it to
 * another thread.</p>
 */
public interface IPostgresEntityOperationContext {

    <EntityType, IdType> Optional<EntityType> find(
            Class<EntityType> entityType,
            IdType id
    );

    <EntityType, IdType> Optional<EntityType> find(
            Class<EntityType> entityType,
            IdType id,
            LockModeType lockModeType
    );

    <EntityType> EntityType save(EntityType entity);

    <EntityType> List<EntityType> saveAll(Collection<EntityType> entities);

    <EntityType> boolean delete(EntityType entity);

    <EntityType, IdType> boolean deleteById(
            Class<EntityType> entityType,
            IdType id
    );

    <EntityType> List<EntityType> findNamed(
            Class<EntityType> entityType,
            String queryName,
            Map<String, ?> parameters,
            int maxResults
    );

    <EntityType> Optional<EntityType> findOneNamed(
            Class<EntityType> entityType,
            String queryName,
            Map<String, ?> parameters
    );

    int executeNamedMutation(
            String queryName,
            Map<String, ?> parameters
    );
}
