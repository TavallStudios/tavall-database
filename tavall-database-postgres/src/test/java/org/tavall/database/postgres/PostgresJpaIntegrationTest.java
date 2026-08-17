package org.tavall.database.postgres;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.entity.IPostgresEntityOperationContext;
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
        IPostgresDatabase database = database("tavall_jpa");

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
    void commitsAtomicEntityOperationsTogether() {
        IPostgresDatabase database = database("tavall_jpa_atomic_commit");

        try {
            database.entities().saveAll(List.of(
                    new JpaProbeEntity("left", "before"),
                    new JpaProbeEntity("right", "before")
            ));

            String result = database.entities().executeAtomic(entities -> {
                JpaProbeEntity left = entities.find(
                        JpaProbeEntity.class,
                        "left",
                        LockModeType.PESSIMISTIC_WRITE
                ).orElseThrow();
                JpaProbeEntity right = entities.find(
                        JpaProbeEntity.class,
                        "right",
                        LockModeType.PESSIMISTIC_WRITE
                ).orElseThrow();

                entities.save(left.withValue("after-left"));
                entities.save(right.withValue("after-right"));
                return "committed";
            });

            assertEquals("committed", result);
            assertEquals(
                    "after-left",
                    database.entities()
                            .find(JpaProbeEntity.class, "left")
                            .orElseThrow()
                            .getValue()
            );
            assertEquals(
                    "after-right",
                    database.entities()
                            .find(JpaProbeEntity.class, "right")
                            .orElseThrow()
                            .getValue()
            );
        } finally {
            database.close();
        }
    }

    @Test
    void clearsManagedStateAfterAtomicNamedMutation() {
        IPostgresDatabase database = database("tavall_jpa_atomic_bulk_mutation");

        try {
            database.entities().save(
                    new JpaProbeEntity("bulk", "before")
            );

            String reloadedValue = database.entities().executeAtomic(entities -> {
                JpaProbeEntity staleManaged = entities.find(
                        JpaProbeEntity.class,
                        "bulk"
                ).orElseThrow();
                assertEquals("before", staleManaged.getValue());

                assertEquals(
                        1,
                        entities.executeNamedMutation(
                                JpaProbeEntity.UPDATE_VALUE_BY_ID,
                                Map.of("id", "bulk", "value", "bulk-updated")
                        )
                );

                return entities.find(
                        JpaProbeEntity.class,
                        "bulk"
                ).orElseThrow().getValue();
            });

            assertEquals("bulk-updated", reloadedValue);
            assertEquals(
                    "bulk-updated",
                    database.entities()
                            .find(JpaProbeEntity.class, "bulk")
                            .orElseThrow()
                            .getValue()
            );
        } finally {
            database.close();
        }
    }

    @Test
    void rejectsAtomicContextUseFromDifferentThread() {
        IPostgresDatabase database = database("tavall_jpa_atomic_thread_scope");

        try {
            database.entities().save(
                    new JpaProbeEntity("thread-scope", "inside")
            );
            AtomicReference<Throwable> workerFailure = new AtomicReference<>();

            database.entities().executeAtomic(entities -> {
                Thread worker = Thread.ofVirtual().start(() -> {
                    try {
                        entities.find(JpaProbeEntity.class, "thread-scope");
                    } catch (Throwable throwable) {
                        workerFailure.set(throwable);
                    }
                });

                try {
                    worker.join(5_000L);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(
                            "Interrupted while verifying atomic context thread scope",
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
    void rejectsEscapedAtomicContextAfterOwnedScopeCompletes() {
        IPostgresDatabase database = database("tavall_jpa_atomic_scope");

        try {
            AtomicReference<IPostgresEntityOperationContext> escaped =
                    new AtomicReference<>();

            database.entities().executeAtomic(entities -> {
                escaped.set(entities);
                entities.save(new JpaProbeEntity("scope", "inside"));
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
    void rollsBackCompleteAtomicEntityOperationAfterFailure() {
        IPostgresDatabase database = database("tavall_jpa_atomic_rollback");

        try {
            database.entities().saveAll(List.of(
                    new JpaProbeEntity("left", "before"),
                    new JpaProbeEntity("right", "before")
            ));

            assertThrows(
                    IllegalStateException.class,
                    () -> database.entities().executeAtomic(entities -> {
                        JpaProbeEntity left = entities.find(
                                JpaProbeEntity.class,
                                "left",
                                LockModeType.PESSIMISTIC_WRITE
                        ).orElseThrow();
                        JpaProbeEntity right = entities.find(
                                JpaProbeEntity.class,
                                "right",
                                LockModeType.PESSIMISTIC_WRITE
                        ).orElseThrow();

                        entities.save(left.withValue("should-rollback-left"));
                        entities.save(right.withValue("should-rollback-right"));
                        throw new IllegalStateException("force rollback");
                    })
            );

            assertEquals(
                    "before",
                    database.entities()
                            .find(JpaProbeEntity.class, "left")
                            .orElseThrow()
                            .getValue()
            );
            assertEquals(
                    "before",
                    database.entities()
                            .find(JpaProbeEntity.class, "right")
                            .orElseThrow()
                            .getValue()
            );
        } finally {
            database.close();
        }
    }

    private IPostgresDatabase database(String name) {
        return PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName(name)
                .generateSchema(true)
                .build()
                .orElseThrow();
    }
}
