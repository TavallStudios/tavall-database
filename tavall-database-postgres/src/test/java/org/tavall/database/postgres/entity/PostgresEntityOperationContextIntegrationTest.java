package org.tavall.database.postgres.entity;

import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.IPostgresDatabase;
import org.tavall.database.postgres.PostgresDatabaseBuilder;
import org.tavall.database.postgres.fixture.JpaProbeEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class PostgresEntityOperationContextIntegrationTest {

    @Test
    void validatesCompleteSaveAllBatchBeforePersistingAnyEntity() {
        IPostgresDatabase database = database(
                "tavall_jpa_atomic_save_all_validation"
        );

        try {
            database.entities().executeAtomic(entities -> {
                assertThrows(
                        IllegalArgumentException.class,
                        () -> entities.saveAll(Arrays.asList(
                                new JpaProbeEntity("first", "should-not-save"),
                                null
                        ))
                );
                return null;
            });

            assertFalse(database.entities().find(
                    JpaProbeEntity.class,
                    "first"
            ).isPresent());
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
