package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.tavall.database.postgres.IPostgresConfigData;

import java.util.Objects;
import java.util.function.Function;

public final class PostgresJpaContext implements IPostgresJpaContext {
    private final IPostgresConfigData configData;
    private final ClassLoader classLoader;
    private final PostgresJpaBootstrap bootstrap;
    private final boolean readOnly;
    private final boolean closeFactoryOnClose;

    private EntityManagerFactory entityManagerFactory;
    private int activeOperations;
    private boolean closeRequested;

    public PostgresJpaContext(
            IPostgresConfigData configData,
            ClassLoader classLoader
    ) {
        this.configData = Objects.requireNonNull(configData, "configData");
        this.classLoader = classLoader;
        this.bootstrap = new PostgresJpaBootstrap();
        this.readOnly = configData.isReadOnly();
        this.closeFactoryOnClose = true;
    }

    private PostgresJpaContext(
            EntityManagerFactory entityManagerFactory,
            boolean closeFactoryOnClose
    ) {
        this.configData = null;
        this.classLoader = null;
        this.bootstrap = null;
        this.readOnly = false;
        this.closeFactoryOnClose = closeFactoryOnClose;
        this.entityManagerFactory = Objects.requireNonNull(
                entityManagerFactory,
                "entityManagerFactory"
        );
    }

    public static PostgresJpaContext owned(EntityManagerFactory entityManagerFactory) {
        return new PostgresJpaContext(entityManagerFactory, true);
    }

    public static PostgresJpaContext borrowed(EntityManagerFactory entityManagerFactory) {
        return new PostgresJpaContext(entityManagerFactory, false);
    }

    @Override
    public EntityManagerFactory entityManagerFactory() {
        synchronized (this) {
            requireOpen();
            return initializeIfNeeded();
        }
    }

    @Override
    public <T> T read(Function<EntityManager, T> operation) {
        Objects.requireNonNull(operation, "operation");
        beginOperation();
        EntityManager entityManager = null;
        try {
            entityManager = initializedFactory().createEntityManager();
            return operation.apply(entityManager);
        } finally {
            try {
                if (entityManager != null && entityManager.isOpen()) {
                    entityManager.close();
                }
            } finally {
                endOperation();
            }
        }
    }

    @Override
    public <T> T write(Function<EntityManager, T> operation) {
        Objects.requireNonNull(operation, "operation");
        if (readOnly) {
            throw new IllegalStateException(
                    "PostgreSQL database is configured as read-only"
            );
        }
        beginOperation();
        EntityManager entityManager = null;
        EntityTransaction transaction = null;
        try {
            entityManager = initializedFactory().createEntityManager();
            transaction = entityManager.getTransaction();
            transaction.begin();
            T result = operation.apply(entityManager);
            entityManager.flush();
            transaction.commit();
            return result;
        } catch (RuntimeException exception) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            throw exception;
        } finally {
            try {
                if (entityManager != null && entityManager.isOpen()) {
                    entityManager.close();
                }
            } finally {
                endOperation();
            }
        }
    }

    @Override
    public synchronized boolean isInitialized() {
        return entityManagerFactory != null;
    }

    @Override
    public synchronized boolean isOpen() {
        return !closeRequested
                && (entityManagerFactory == null || entityManagerFactory.isOpen());
    }

    @Override
    public synchronized void close() {
        closeRequested = true;
        closeFactoryIfDrained();
    }

    private EntityManagerFactory initializedFactory() {
        synchronized (this) {
            return initializeIfNeeded();
        }
    }

    private EntityManagerFactory initializeIfNeeded() {
        requireOpen();
        if (entityManagerFactory == null) {
            if (bootstrap == null || configData == null) {
                throw new IllegalStateException(
                        "PostgreSQL JPA context has no factory or bootstrap configuration"
                );
            }
            entityManagerFactory = bootstrap.createEntityManagerFactory(
                    configData,
                    classLoader
            );
        }
        if (!entityManagerFactory.isOpen()) {
            throw new IllegalStateException(
                    "PostgreSQL JPA EntityManagerFactory is closed"
            );
        }
        return entityManagerFactory;
    }

    private synchronized void beginOperation() {
        requireOpen();
        activeOperations++;
    }

    private synchronized void endOperation() {
        activeOperations--;
        closeFactoryIfDrained();
    }

    private void requireOpen() {
        if (closeRequested) {
            throw new IllegalStateException("PostgreSQL JPA context is closed");
        }
    }

    private void closeFactoryIfDrained() {
        if (closeFactoryOnClose
                && closeRequested
                && activeOperations == 0
                && entityManagerFactory != null
                && entityManagerFactory.isOpen()) {
            entityManagerFactory.close();
        }
    }
}
