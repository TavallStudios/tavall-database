package org.tavall.database.postgres.entity;

import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.tavall.database.postgres.IPostgresDatabase;
import org.tavall.database.postgres.PostgresDatabaseBuilder;
import org.tavall.database.postgres.fixture.JpaProbeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * PostgreSQL-specific acceptance for the typed entity-store transaction boundary.
 *
 * <p>The regular JPA suite keeps lightweight provider-independent coverage.
 * These tests exercise the transaction contract against an actual PostgreSQL
 * service supplied by Tavall/local validation infrastructure so pessimistic
 * locking, commit, and rollback evidence is never inferred from H2.</p>
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
}
