package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.function.Function;

/**
 * Owns the PostgreSQL JPA factory and the resource-local transaction boundary.
 *
 * <p>Consumers receive standard Jakarta Persistence types. Provider-specific
 * bootstrap and lifecycle behavior remain inside tavall-database.</p>
 */
public interface IPostgresJpaContext extends AutoCloseable {

    EntityManagerFactory entityManagerFactory();

    <T> T read(Function<EntityManager, T> operation);

    <T> T write(Function<EntityManager, T> operation);

    boolean isInitialized();

    boolean isOpen();

    @Override
    void close();
}
