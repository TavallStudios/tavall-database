package org.tavall.database.postgres.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

final class PostgresEntityTransaction implements IPostgresEntityTransaction {
    private final EntityManager entityManager;
    private final Thread ownerThread;
    private volatile boolean active = true;

    PostgresEntityTransaction(EntityManager entityManager) {
        this.entityManager = Objects.requireNonNull(
                entityManager,
                "entityManager"
        );
        this.ownerThread = Thread.currentThread();
    }

    @Override
    public <E, I> Optional<E> find(Class<E> entityType, I id) {
        return find(entityType, id, LockModeType.NONE);
    }

    @Override
    public <E, I> Optional<E> find(
            Class<E> entityType,
            I id,
            LockModeType lockModeType
    ) {
        ensureActive();
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(id, "id");
        LockModeType safeLockMode = lockModeType == null
                ? LockModeType.NONE
                : lockModeType;
        if (safeLockMode == LockModeType.NONE) {
            return Optional.ofNullable(entityManager.find(entityType, id));
        }
        return Optional.ofNullable(entityManager.find(
                entityType,
                id,
                safeLockMode
        ));
    }

    @Override
    public <E> E save(E entity) {
        ensureActive();
        Objects.requireNonNull(entity, "entity");
        return entityManager.merge(entity);
    }

    @Override
    public <E> List<E> saveAll(Collection<E> entities) {
        ensureActive();
        Objects.requireNonNull(entities, "entities");
        ArrayList<E> saved = new ArrayList<>(entities.size());
        for (E entity : entities) {
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
    public <E> boolean delete(E entity) {
        ensureActive();
        Objects.requireNonNull(entity, "entity");
        E managed = entityManager.contains(entity)
                ? entity
                : entityManager.merge(entity);
        entityManager.remove(managed);
        return true;
    }

    @Override
    public <E, I> boolean deleteById(Class<E> entityType, I id) {
        ensureActive();
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(id, "id");
        E entity = entityManager.find(
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
    public <E> List<E> findNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters,
            int maxResults
    ) {
        ensureActive();
        Objects.requireNonNull(entityType, "entityType");
        String safeQueryName = requireText(queryName, "queryName");
        TypedQuery<E> query = entityManager.createNamedQuery(
                safeQueryName,
                entityType
        );
        bind(query, copyParameters(parameters));
        if (maxResults > 0) {
            query.setMaxResults(maxResults);
        }
        return List.copyOf(query.getResultList());
    }

    @Override
    public <E> Optional<E> findOneNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters
    ) {
        return findNamed(
                entityType,
                queryName,
                parameters,
                1
        ).stream().findFirst();
    }

    @Override
    public int executeNamedMutation(
            String queryName,
            Map<String, ?> parameters
    ) {
        ensureActive();
        String safeQueryName = requireText(queryName, "queryName");
        Query query = entityManager.createNamedQuery(safeQueryName);
        bind(query, copyParameters(parameters));
        return query.executeUpdate();
    }

    void invalidate() {
        active = false;
    }

    private void ensureActive() {
        if (!active) {
            throw new IllegalStateException(
                    "Entity transaction scope is no longer active"
            );
        }
        if (Thread.currentThread() != ownerThread) {
            throw new IllegalStateException(
                    "Entity transaction scope is bound to its owning thread"
            );
        }
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
        return java.util.Collections.unmodifiableMap(copied);
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
