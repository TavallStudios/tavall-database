package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.tavall.database.postgres.IPostgresDatabase;
import org.tavall.database.postgres.PostgresDatabaseBuilder;
import org.tavall.database.postgres.fixture.JpaProbeEntity;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * PostgreSQL-specific acceptance for the typed entity-store transaction boundary.
 *
 * <p>The regular JPA suite keeps lightweight provider-independent coverage.
 * These tests exercise the transaction contract against an actual PostgreSQL
 * service supplied by Tavall/local validation infrastructure so pessimistic
 * locking, commit, rollback, and concurrent serialization evidence is never
 * inferred from H2.</p>
 */
@EnabledIfEnvironmentVariable(
        named = "TAVALL_TEST_POSTGRES_JDBC_URL",
        matches = "jdbc:postgresql:.+"
)
final class PostgresEntityStorePostgresIntegrationTest {

    @Test
    void commitsLockedMutationAndAuditInOnePostgresTransaction() {
        IPostgresDatabase database = postgresDatabase(
                "tavall-postgres-entity-operation"
        );

        try {
            database.entities().save(
                    new JpaProbeEntity("postgres-account", "before")
            );

            String result = database.entities().execute(transaction -> {
                JpaProbeEntity locked = transaction.find(
                        JpaProbeEntity.class,
                        "postgres-account",
                        LockModeType.PESSIMISTIC_WRITE
                ).orElseThrow();
                transaction.save(locked.withValue("after"));
                transaction.save(new JpaProbeEntity(
                        "postgres-audit",
                        "committed"
                ));
                return transaction.find(
                        JpaProbeEntity.class,
                        "postgres-account"
                ).orElseThrow().getValue();
            });

            assertEquals("after", result);
            assertEquals(
                    "after",
                    database.entities()
                            .find(JpaProbeEntity.class, "postgres-account")
                            .orElseThrow()
                            .getValue()
            );
            assertEquals(
                    "committed",
                    database.entities()
                            .find(JpaProbeEntity.class, "postgres-audit")
                            .orElseThrow()
                            .getValue()
            );
        } finally {
            database.close();
        }
    }

    @Test
    void rollsBackEveryWriteWhenPostgresOperationFails() {
        IPostgresDatabase database = postgresDatabase(
                "tavall-postgres-entity-rollback"
        );

        try {
            assertThrows(
                    IllegalStateException.class,
                    () -> database.entities().execute(transaction -> {
                        transaction.save(new JpaProbeEntity(
                                "postgres-rolled-back-account",
                                "changed"
                        ));
                        transaction.save(new JpaProbeEntity(
                                "postgres-rolled-back-audit",
                                "changed"
                        ));
                        throw new IllegalStateException("fail operation");
                    })
            );

            assertFalse(database.entities().find(
                    JpaProbeEntity.class,
                    "postgres-rolled-back-account"
            ).isPresent());
            assertFalse(database.entities().find(
                    JpaProbeEntity.class,
                    "postgres-rolled-back-audit"
            ).isPresent());
        } finally {
            database.close();
        }
    }

    @Test
    void serializesConcurrentPessimisticWriteOperations() throws Exception {
        IPostgresDatabase database = postgresDatabase(
                "tavall-postgres-entity-concurrency"
        );

        try {
            database.entities().save(
                    new JpaProbeEntity("postgres-concurrent-account", "0")
            );

            CountDownLatch firstLockAcquired = new CountDownLatch(1);
            CountDownLatch releaseFirstOperation = new CountDownLatch(1);
            CountDownLatch secondAttemptingLock = new CountDownLatch(1);
            CountDownLatch secondLockAcquired = new CountDownLatch(1);
            AtomicReference<Throwable> firstFailure = new AtomicReference<>();
            AtomicReference<Throwable> secondFailure = new AtomicReference<>();

            Thread firstOperation = Thread.ofVirtual().start(() -> {
                try {
                    database.entities().execute(transaction -> {
                        JpaProbeEntity locked = transaction.find(
                                JpaProbeEntity.class,
                                "postgres-concurrent-account",
                                LockModeType.PESSIMISTIC_WRITE
                        ).orElseThrow();
                        firstLockAcquired.countDown();
                        await(releaseFirstOperation);
                        transaction.save(locked.withValue("1"));
                        return null;
                    });
                } catch (Throwable throwable) {
                    firstFailure.set(throwable);
                }
            });

            assertTrue(firstLockAcquired.await(5, TimeUnit.SECONDS));

            Thread secondOperation = Thread.ofVirtual().start(() -> {
                try {
                    database.entities().execute(transaction -> {
                        secondAttemptingLock.countDown();
                        JpaProbeEntity locked = transaction.find(
                                JpaProbeEntity.class,
                                "postgres-concurrent-account",
                                LockModeType.PESSIMISTIC_WRITE
                        ).orElseThrow();
                        secondLockAcquired.countDown();
                        int nextValue = Integer.parseInt(locked.getValue()) + 1;
                        transaction.save(locked.withValue(Integer.toString(nextValue)));
                        return null;
                    });
                } catch (Throwable throwable) {
                    secondFailure.set(throwable);
                }
            });

            assertTrue(secondAttemptingLock.await(5, TimeUnit.SECONDS));
            assertFalse(secondLockAcquired.await(250, TimeUnit.MILLISECONDS));

            releaseFirstOperation.countDown();
            firstOperation.join(5_000L);
            secondOperation.join(5_000L);

            assertFalse(firstOperation.isAlive());
            assertFalse(secondOperation.isAlive());
            assertNull(firstFailure.get());
            assertNull(secondFailure.get());
            assertTrue(secondLockAcquired.await(1, TimeUnit.SECONDS));
            assertEquals(
                    "2",
                    database.entities()
                            .find(JpaProbeEntity.class, "postgres-concurrent-account")
                            .orElseThrow()
                            .getValue()
            );
        } finally {
            database.close();
        }
    }

    private IPostgresDatabase postgresDatabase(String persistenceUnitName) {
        return PostgresDatabaseBuilder.create()
                .jdbcUrl(requireEnvironment("TAVALL_TEST_POSTGRES_JDBC_URL"))
                .username(environmentOrDefault(
                        "TAVALL_TEST_POSTGRES_USERNAME",
                        "postgres"
                ))
                .password(environmentOrDefault(
                        "TAVALL_TEST_POSTGRES_PASSWORD",
                        "postgres"
                ))
                .persistenceUnitName(persistenceUnitName)
                .generateSchema(true)
                .build()
                .orElseThrow();
    }

    private String requireEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required");
        }
        return value.trim();
    }

    private String environmentOrDefault(String name, String fallback) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim();
    }

    private void await(CountDownLatch latch) {
        try {
            if (!latch.await(5, TimeUnit.SECONDS)) {
                throw new IllegalStateException("Timed out waiting for test coordination");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for test coordination", exception);
        }
    }
}
