package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.tavall.database.postgres.exception.PostgresJpaException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

final class PostgresJpaHandler implements IPostgresJpaHandler {

    private final EntityManagerFactory entityManagerFactory;
    private final Runnable providerClose;
    private final boolean readOnly;
    private final AtomicBoolean closed = new AtomicBoolean();

    PostgresJpaHandler(
            EntityManagerFactory entityManagerFactory,
            Runnable providerClose,
            boolean readOnly
    ) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory");
        this.providerClose = Objects.requireNonNull(providerClose, "providerClose");
        this.readOnly = readOnly;
    }

    @Override
    public boolean isConfigured() {
        return true;
    }

    @Override
    public boolean isOpen() {
        return !closed.get() && entityManagerFactory.isOpen();
    }

    @Override
    public <T> T read(Function<EntityManager, T> operation) {
        return execute("read", false, operation);
    }

    @Override
    public <T> T write(Function<EntityManager, T> operation) {
        if (readOnly) {
            throw new PostgresJpaException("PostgreSQL JPA writes are disabled because the database is read-only.");
        }
        return execute("write", true, operation);
    }

    private <T> T execute(
            String operationName,
            boolean flushBeforeCommit,
            Function<EntityManager, T> operation
    ) {
        Objects.requireNonNull(operation, "operation");
        ensureOpen();

        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        Throwable failure = null;
        boolean userOperationStarted = false;
        boolean userOperationCompleted = false;
        try {
            entityManager = entityManagerFactory.createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();

            userOperationStarted = true;
            T result = operation.apply(entityManager);
            userOperationCompleted = true;

            if (flushBeforeCommit) {
                entityManager.flush();
            }
            transaction.commit();
            return result;
        } catch (RuntimeException | Error exception) {
            failure = exception;
            rollback(transaction, exception);
            if (userOperationStarted && !userOperationCompleted) {
                throw exception;
            }

            PostgresJpaException transactionFailure = new PostgresJpaException(
                    "Unable to complete PostgreSQL JPA " + operationName + " transaction.",
                    exception
            );
            failure = transactionFailure;
            throw transactionFailure;
        } finally {
            closeEntityManager(entityManager, failure);
        }
    }

    private void ensureOpen() {
        if (!isOpen()) {
            throw new PostgresJpaException("PostgreSQL JPA is closed.");
        }
    }

    private void rollback(EntityTransaction transaction, Throwable failure) {
        if (transaction == null) {
            return;
        }

        try {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } catch (RuntimeException rollbackFailure) {
            failure.addSuppressed(rollbackFailure);
        }
    }

    private void closeEntityManager(EntityManager entityManager, Throwable activeFailure) {
        if (entityManager == null) {
            return;
        }

        try {
            if (entityManager.isOpen()) {
                entityManager.close();
            }
        } catch (RuntimeException closeFailure) {
            if (activeFailure != null) {
                activeFailure.addSuppressed(closeFailure);
                return;
            }
            throw new PostgresJpaException("Unable to close the PostgreSQL JPA entity manager.", closeFailure);
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }

        RuntimeException failure = null;
        try {
            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
        } catch (RuntimeException closeFailure) {
            failure = closeFailure;
        }

        try {
            providerClose.run();
        } catch (RuntimeException providerFailure) {
            if (failure == null) {
                failure = providerFailure;
            } else {
                failure.addSuppressed(providerFailure);
            }
        }

        if (failure != null) {
            throw new PostgresJpaException("Unable to close PostgreSQL JPA.", failure);
        }
    }
}
