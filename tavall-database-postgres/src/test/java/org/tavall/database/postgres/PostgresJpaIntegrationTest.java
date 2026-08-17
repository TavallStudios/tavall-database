package org.tavall.database.postgres;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.entity.IPostgresEntityTransaction;
import org.tavall.database.postgres.fixture.JpaProbeEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostgresJpaIntegrationTest {

    @Test
    void discoversTavallEntitiesAndOwnsEntityOperations() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_jpa;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-jpa-integration")
                .generateSchema(true)
                .build()
                .orElseThrow();

        try {
            assertTrue(database.entities().isOpen());

            database.entities().save(new JpaProbeEntity("probe", "ready"));
            database.entities().saveAll(List.of(
                    new JpaProbeEntity("second", "ready"),
                    new JpaProbeEntity("third", "other")
            ));

            assertEquals(
                    "ready",
                    database.entities()
                            .find(JpaProbeEntity.class, "probe")
                            .orElseThrow()
                            .getValue()
            );
            assertEquals(
                    List.of("probe", "second"),
                    database.entities().findNamed(
                                    JpaProbeEntity.class,
                                    JpaProbeEntity.FIND_BY_VALUE,
                                    Map.of("value", "ready"),
                                    25
                            ).stream()
                            .map(JpaProbeEntity::getId)
                            .toList()
            );

            database.entities().save(
                    new JpaProbeEntity("probe", "updated")
            );
            assertEquals(
                    "updated",
                    database.entities()
                            .find(JpaProbeEntity.class, "probe")
                            .orElseThrow()
                            .getValue()
            );

            assertTrue(database.entities().deleteById(
                    JpaProbeEntity.class,
                    "second"
            ));
            assertFalse(database.entities().find(
                    JpaProbeEntity.class,
                    "second"
            ).isPresent());
        } finally {
            database.close();
        }

        assertFalse(database.entities().isOpen());
    }

    @Test
    void executesTypedEntityOperationInOneOwnedTransaction() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_entity_operation;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-entity-operation")
                .generateSchema(true)
                .build()
                .orElseThrow();

        try {
            database.entities().save(
                    new JpaProbeEntity("account", "before")
            );

            String result = database.entities().execute(transaction -> {
                JpaProbeEntity locked = transaction.find(
                        JpaProbeEntity.class,
                        "account",
                        LockModeType.PESSIMISTIC_WRITE
                ).orElseThrow();
                transaction.save(locked.withValue("after"));
                transaction.save(new JpaProbeEntity("audit", "committed"));
                return transaction.find(
                        JpaProbeEntity.class,
                        "account"
                ).orElseThrow().getValue();
            });

            assertEquals("after", result);
            assertEquals(
                    "after",
                    database.entities()
                            .find(JpaProbeEntity.class, "account")
                            .orElseThrow()
                            .getValue()
            );
            assertEquals(
                    "committed",
                    database.entities()
                            .find(JpaProbeEntity.class, "audit")
                            .orElseThrow()
                            .getValue()
            );
        } finally {
            database.close();
        }
    }

    @Test
    void rejectsEntityTransactionUseFromDifferentThread() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_entity_thread_scope;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-entity-thread-scope")
                .generateSchema(true)
                .build()
                .orElseThrow();

        try {
            database.entities().save(
                    new JpaProbeEntity("thread-scope", "inside")
            );
            AtomicReference<Throwable> workerFailure = new AtomicReference<>();

            database.entities().execute(transaction -> {
                Thread worker = Thread.ofVirtual().start(() -> {
                    try {
                        transaction.find(
                                JpaProbeEntity.class,
                                "thread-scope"
                        );
                    } catch (Throwable throwable) {
                        workerFailure.set(throwable);
                    }
                });

                try {
                    worker.join(5_000L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "Interrupted while verifying transaction thread scope",
                            exception
                    );
                }
                assertFalse(worker.isAlive());
                return null;
            });

            assertTrue(workerFailure.get() instanceof IllegalStateException);
        } finally {
            database.close();
        }
    }

    @Test
    void rejectsEscapedEntityTransactionAfterOwnedScopeCompletes() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_entity_scope;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-entity-scope")
                .generateSchema(true)
                .build()
                .orElseThrow();

        try {
            AtomicReference<IPostgresEntityTransaction> escaped =
                    new AtomicReference<>();

            database.entities().execute(transaction -> {
                escaped.set(transaction);
                transaction.save(new JpaProbeEntity("scope", "inside"));
                return null;
            });

            assertEquals(
                    "inside",
                    database.entities()
                            .find(JpaProbeEntity.class, "scope")
                            .orElseThrow()
                            .getValue()
            );
            assertThrows(
                    IllegalStateException.class,
                    () -> escaped.get().find(JpaProbeEntity.class, "scope")
            );
            assertThrows(
                    IllegalStateException.class,
                    () -> escaped.get().save(
                            new JpaProbeEntity("late", "outside")
                    )
            );
        } finally {
            database.close();
        }
    }

    @Test
    void rollsBackTypedEntityOperationWhenApplicationOperationFails() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_entity_rollback;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-entity-rollback")
                .generateSchema(true)
                .build()
                .orElseThrow();

        try {
            assertThrows(
                    IllegalStateException.class,
                    () -> database.entities().execute(transaction -> {
                        transaction.save(new JpaProbeEntity(
                                "rolled-back-account",
                                "changed"
                        ));
                        transaction.save(new JpaProbeEntity(
                                "rolled-back-audit",
                                "changed"
                        ));
                        throw new IllegalStateException("fail operation");
                    })
            );

            assertFalse(database.entities().find(
                    JpaProbeEntity.class,
                    "rolled-back-account"
            ).isPresent());
            assertFalse(database.entities().find(
                    JpaProbeEntity.class,
                    "rolled-back-audit"
            ).isPresent());
        } finally {
            database.close();
        }
    }
}
