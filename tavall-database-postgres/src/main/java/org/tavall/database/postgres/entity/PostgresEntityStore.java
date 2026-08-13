package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;
import org.tavall.database.postgres.jpa.IPostgresJpaContext;

import java.util.Collection;
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
        LockModeType safeLockMode = lockModeType == null
                ? LockModeType.NONE
                : lockModeType;
        if (safeLockMode == LockModeType.NONE) {
            return jpaContext.read(entityManager -> operationContext(entityManager)
                    .find(entityType, id, safeLockMode));
        }
        return jpaContext.write(entityManager -> operationContext(entityManager)
                .find(entityType, id, safeLockMode));
    }

    @Override
    public <E> E save(E entity) {
        return jpaContext.write(entityManager -> operationContext(entityManager)
                .save(entity));
    }

    @Override
    public <E> List<E> saveAll(Collection<E> entities) {
        return jpaContext.write(entityManager -> operationContext(entityManager)
                .saveAll(entities));
    }

    @Override
    public <E> boolean delete(E entity) {
        return jpaContext.write(entityManager -> operationContext(entityManager)
                .delete(entity));
    }

    @Override
    public <E, I> boolean deleteById(Class<E> entityType, I id) {
        return jpaContext.write(entityManager -> operationContext(entityManager)
                .deleteById(entityType, id));
    }

    @Override
    public <E> List<E> findNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters,
            int maxResults
    ) {
        return jpaContext.read(entityManager -> operationContext(entityManager)
                .findNamed(entityType, queryName, parameters, maxResults));
    }

    @Override
    public <E> Optional<E> findOneNamed(
            Class<E> entityType,
            String queryName,
            Map<String, ?> parameters
    ) {
        return jpaContext.read(entityManager -> operationContext(entityManager)
                .findOneNamed(entityType, queryName, parameters));
    }

    @Override
    public int executeNamedMutation(
            String queryName,
            Map<String, ?> parameters
    ) {
        return jpaContext.write(entityManager -> operationContext(entityManager)
                .executeNamedMutation(queryName, parameters));
    }

    @Override
    public <ResultType> ResultType executeAtomic(
            IPostgresEntityAtomicOperation<ResultType> operation
    ) {
        Objects.requireNonNull(operation, "operation");
        return jpaContext.write(entityManager -> operation.execute(
                operationContext(entityManager)
        ));
    }

    private IPostgresEntityOperationContext operationContext(
            jakarta.persistence.EntityManager entityManager
    ) {
        return new PostgresEntityOperationContext(entityManager);
    }
}
