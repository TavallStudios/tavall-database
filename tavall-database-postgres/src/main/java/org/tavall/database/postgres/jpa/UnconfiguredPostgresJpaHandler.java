package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;
import org.tavall.database.postgres.exception.PostgresJpaException;

import java.util.function.Function;

final class UnconfiguredPostgresJpaHandler implements IPostgresJpaHandler {

    static final UnconfiguredPostgresJpaHandler INSTANCE = new UnconfiguredPostgresJpaHandler();

    private UnconfiguredPostgresJpaHandler() {
    }

    @Override
    public boolean isConfigured() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public <T> T read(Function<EntityManager, T> operation) {
        throw unavailable();
    }

    @Override
    public <T> T write(Function<EntityManager, T> operation) {
        throw unavailable();
    }

    @Override
    public void close() {
    }

    private PostgresJpaException unavailable() {
        return new PostgresJpaException(
                "PostgreSQL JPA is not configured. Register at least one entity class or entity package on the database builder."
        );
    }
}
