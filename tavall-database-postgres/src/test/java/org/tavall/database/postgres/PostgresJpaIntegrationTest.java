package org.tavall.database.postgres;

import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.fixture.JpaProbeEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostgresJpaIntegrationTest {

    @Test
    void scansEntitiesAndOwnsEntityOperations() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_jpa;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-jpa-integration")
                .entityPackage("org.tavall.database.postgres.fixture")
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
}
