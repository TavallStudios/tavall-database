package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public interface IPostgresJpaHandler extends AutoCloseable {

    boolean isConfigured();

    boolean isOpen();

    <T> T read(Function<EntityManager, T> operation);

    default void readVoid(Consumer<EntityManager> operation) {
        Objects.requireNonNull(operation, "operation");
        read(entityManager -> {
            operation.accept(entityManager);
            return null;
        });
    }

    <T> T write(Function<EntityManager, T> operation);

    default void writeVoid(Consumer<EntityManager> operation) {
        Objects.requireNonNull(operation, "operation");
        write(entityManager -> {
            operation.accept(entityManager);
            return null;
        });
    }

    @Override
    void close();
}
