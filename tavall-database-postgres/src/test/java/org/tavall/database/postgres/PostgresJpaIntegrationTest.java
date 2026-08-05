package org.tavall.database.postgres;

import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.fixture.JpaProbeEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostgresJpaIntegrationTest {

    @Test
    void scansEntitiesAndOwnsReadWriteTransactions() {
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
            assertFalse(database.jpa().isInitialized());

            database.jpa().write(entityManager -> {
                entityManager.persist(new JpaProbeEntity("probe", "ready"));
                return null;
            });

            assertTrue(database.jpa().isInitialized());
            String value = database.jpa().read(entityManager ->
                    entityManager.find(JpaProbeEntity.class, "probe").getValue()
            );
            assertEquals("ready", value);
        } finally {
            database.close();
        }

        assertFalse(database.jpa().isOpen());
    }
}
