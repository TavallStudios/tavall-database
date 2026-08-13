package org.tavall.database.postgres.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class PostgresEntityOperationContext
        implements IPostgresEntityOperationContext {

    private final EntityManager entityManager;

    PostgresEntityOperationContext(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(
                entityManager,
                "entityManager"
        );
    }

    @Override
    public <EntityType, IdType> Optional<EntityType> find(
            Class<EntityType> entityType,
            IdType id
    ) {
        return find(entityType, id, LockModeType.NONE);
    }

    @Override
    public <EntityType, IdType> Optional<EntityType> find(
            Class<EntityType> entityType,
            IdType id,
            LockModeType lockModeType
    ) {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(id, "id");
        LockModeType safeLockMode = lockModeType == null
                ? LockModeType.NONE
                : lockModeType;
        EntityType entity = safeLockMode == LockModeType.NONE
                ? entityManager.find(entityType, id)
                : entityManager.find(entityType, id, safeLockMode);
        return Optional.ofNullable(entity);
    }

    @Override
    public <EntityType> EntityType save(EntityType entity) {
        Objects.requireNonNull(entity, "entity");
        return entityManager.merge(entity);
    }

    @Override
    public <EntityType> List<EntityType> saveAll(
            Collection<EntityType> entities
    ) {
        Objects.requireNonNull(entities, "entities");
        ArrayList<EntityType> saved = new ArrayList<>(entities.size());
        for (EntityType entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException(
                        "entities must not contain null"
                );
            }
            saved.add(entityManager.merge(entity));
        }
        return List.copyOf(saved);
    }

    @Override
    public <EntityType> boolean delete(EntityType entity) {
        Objects.requireNonNull(entity, "entity");
        EntityType managed = entityManager.contains(entity)
                ? entity
                : entityManager.merge(entity);
        entityManager.remove(managed);
        return true;
    }

    @Override
    public <EntityType, IdType> boolean deleteById(
            Class<EntityType> entityType,
            IdType id
    ) {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(id, "id");
        EntityType entity = entityManager.find(
                entityType,
                id,
                LockModeType.PESSIMISTIC_WRITE
        );
        if (entity == null) {
            return false;
        }
        entityManager.remove(entity);
        return true;
    }

    @Override
    public <EntityType> List<EntityType> findNamed(
            Class<EntityType> entityType,
            String queryName,
            Map<String, ?> parameters,
            int maxResults
    ) {
        Objects.requireNonNull(entityType, "entityType");
        String safeQueryName = requireText(queryName, "queryName");
        Map<String, ?> safeParameters = copyParameters(parameters);
        int safeMaxResults = maxResults <= 0
                ? Integer.MAX_VALUE
                : maxResults;
        TypedQuery<EntityType> query = entityManager.createNamedQuery(
                safeQueryName,
                entityType
        );
        bind(query, safeParameters);
        if (safeMaxResults != Integer.MAX_VALUE) {
            query.setMaxResults(safeMaxResults);
        }
        return List.copyOf(query.getResultList());
    }

    @Override
    public <EntityType> Optional<EntityType> findOneNamed(
            Class<EntityType> entityType,
            String queryName,
            Map<String, ?> parameters
    ) {
        List<EntityType> results = findNamed(
                entityType,
                queryName,
                parameters,
                1
        );
        return results.isEmpty()
                ? Optional.empty()
                : Optional.of(results.getFirst());
    }

    @Override
    public int executeNamedMutation(
            String queryName,
            Map<String, ?> parameters
    ) {
        String safeQueryName = requireText(queryName, "queryName");
        Map<String, ?> safeParameters = copyParameters(parameters);
        Query query = entityManager.createNamedQuery(safeQueryName);
        bind(query, safeParameters);
        return query.executeUpdate();
    }

    private Map<String, ?> copyParameters(Map<String, ?> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Map.of();
        }
        LinkedHashMap<String, Object> copied = new LinkedHashMap<>();
        parameters.forEach((name, value) -> copied.put(
                requireText(name, "parameter name"),
                value
        ));
        return Collections.unmodifiableMap(copied);
    }

    private void bind(Query query, Map<String, ?> parameters) {
        parameters.forEach(query::setParameter);
    }

    private String requireText(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " is required");
        }
        return value.trim();
    }
}
