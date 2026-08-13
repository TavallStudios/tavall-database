package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.function.Function;

/**
 * Owns the PostgreSQL JPA factory, entity-manager lifetime, and resource-local transaction boundary.
 *
 * <p>Consumers receive standard Jakarta Persistence types. Provider-specific bootstrap and
 * lifecycle behavior remain inside tavall-database. Read operations receive a fresh
 * {@link EntityManager} without an explicit transaction. Write operations receive a fresh entity
 * manager inside one resource-local transaction that is flushed and committed when the callback
 * succeeds and rolled back when a runtime failure escapes.</p>
 *
 * <p>Closing the context rejects new operations immediately. Contexts that own their
 * {@link EntityManagerFactory} defer factory closure until already-running operations drain;
 * borrowed-factory contexts leave the external factory open.</p>
 */
public interface IPostgresJpaContext extends AutoCloseable {

    /**
     * Returns the underlying entity-manager factory, initializing it lazily when necessary.
     *
     * @return open entity-manager factory used by this context
     * @throws IllegalStateException if the context has been closed or the factory cannot be made
     *                               available
     */
    EntityManagerFactory entityManagerFactory();

    /**
     * Executes a read operation with a fresh entity manager owned by this context.
     *
     * <p>The entity manager is closed after the callback returns or throws. No resource-local
     * transaction is started by this method.</p>
     *
     * @param operation callback that performs the read
     * @param <T> callback result type
     * @return value returned by the callback
     * @throws NullPointerException if {@code operation} is {@code null}
     * @throws IllegalStateException if the context is closed or cannot initialize its factory
     */
    <T> T read(Function<EntityManager, T> operation);

    /**
     * Executes a write operation inside one resource-local transaction.
     *
     * <p>On success the context flushes the entity manager and commits before returning the
     * callback result. A runtime exception triggers rollback while the transaction remains active,
     * then the original failure is propagated. The entity manager is closed in all cases.</p>
     *
     * @param operation callback that performs the transactional write
     * @param <T> callback result type
     * @return value returned after a successful commit
     * @throws NullPointerException if {@code operation} is {@code null}
     * @throws IllegalStateException if the context is closed, cannot initialize, or is configured
     *                               read-only
     */
    <T> T write(Function<EntityManager, T> operation);

    /**
     * Reports whether this context has already created or been given an entity-manager factory.
     *
     * @return {@code true} once a factory reference has been established
     */
    boolean isInitialized();

    /**
     * Reports whether the context accepts new operations.
     *
     * @return {@code true} while close has not been requested and any initialized factory remains
     *         open
     */
    boolean isOpen();

    /**
     * Requests context closure.
     *
     * <p>New operations are rejected immediately. An owned factory is closed after active
     * operations drain; a borrowed factory remains externally owned and is not closed here.</p>
     */
    @Override
    void close();
}
