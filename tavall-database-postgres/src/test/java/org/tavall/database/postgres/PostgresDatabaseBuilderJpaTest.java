package org.tavall.database.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostgresDatabaseBuilderJpaTest {

    @Test
    void buildComposesJpaWithoutOpeningPersistence() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:postgresql://127.0.0.1:1/unused")
                .username("unused")
                .password("unused")
                .persistenceUnitName("lazy-test")
                .entityPackage("org.tavall.database.postgres.fixture")
                .build()
                .orElseThrow();

        try {
            assertNotNull(database.jpa());
            assertTrue(database.jpa().isOpen());
            assertFalse(database.jpa().isInitialized());
        } finally {
            database.close();
        }

        assertFalse(database.jpa().isOpen());
        assertFalse(database.jpa().isInitialized());
    }
}
