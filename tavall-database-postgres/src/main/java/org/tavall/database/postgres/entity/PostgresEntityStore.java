package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import org.tavall.database.postgres.jpa.IPostgresJpaContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class PostgresEntityStore implements IPostgresEntityStore {
    private final IPostgresJpaContext jpaContext;

    public PostgresEntityStore(IPostgresJpaContext jpaContext) {
        this.jpaContext = Objects.requireNonNull(jpaContext, "jpaContext");
    }

    @Override
    public boolean isOpen() {
        return jpaContext.isOpen();
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
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(id, "id");
        LockModeType safeLockMode = lockModeType == null
                ? LockModeType.NONE
                : lockModeType;
        if (safeLockMode == LockModeType.NONE) {
            return jpaContext.read(entityManager -> Optional.ofNullable(
                    entityManager.find(entityType, id)
            ));
        }
        return jpaContext.write(entityManager -> Optional.ofNullable(
                entityManager.find(entityType, id, safeLockMode)
        ));
    }

    @Override
    public <E> E save(E entity) {
        Objects.requireNonNull(entity, "entity");
        return jpaContext.write(entityManager -> entityManager.merge(entity));
    }

    @Override
    public <E> List<E> saveAll(Collection<E> entities) {
        Objects.requireNonNull(entities, "entities");
        ArrayList<E> values = new ArrayList<>(entities.size());
        for (E entity : entities) {
            if (entity == null) {
                throw new IllegalArgumentException(
                        "entities must not contain null"
                );
            }
            values.add(entity);
        }
        return jpaContext.write(entityManager -> {
            ArrayList<E> saved = new ArrayList<>(values.size());
            for (E entity : values) {
                saved.add(entityManager.merge(entity));
            }
            return List.copyOf(saved);
        });
    }

    @Override
    public <E> boolean delete(E entity) {
        Objects.requireNonNull(entity, "entity");
        return jpaContext.write(entityManager -> {
            E managed = entityManager.contains(entity)
                    ? entity
                    : entityManager.merge(entity);
            entityManager.remove(managed);
            return true;
        });
    }

    @Override
    public <E, I> boolean deleteById(Class<E> entityType, I id) {
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(id, "id");
        return jpaContext.write(entityManager -> {
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
        });
    }

    @Override
    public <E> List<E> findNamed(
            Class<E> entityType,
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
        return jpaContext.read(entityManager -> {
            TypedQuery<E> query = entityManager.createNamedQuery(
                    safeQueryName,
                    entityType
            );
            bind(query, safeParameters);
            if (safeMaxResults != Integer.MAX_VALUE) {
                query.setMaxResults(safeMaxResults);
            }
            return List.copyOf(query.getResultList());
        });
    }

    @Override
    public <E> Optional<E> findOneNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters
    ) {
        List<E> results = findNamed(
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
        return jpaContext.write(entityManager -> {
            Query query = entityManager.createNamedQuery(safeQueryName);
            bind(query, safeParameters);
            return query.executeUpdate();
        });
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
