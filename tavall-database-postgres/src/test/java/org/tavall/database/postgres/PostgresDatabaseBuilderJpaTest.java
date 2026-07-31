package org.tavall.database.postgres;

import org.junit.jupiter.api.Test;
import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.postgres.exception.PostgresJpaException;
import org.tavall.database.postgres.jpa.fixtures.TestJpaEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresDatabaseBuilderJpaTest {

    @Test
    void preservesJdbcOnlyConstructionWhenJpaIsNotConfigured() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:postgresql://localhost/test")
                .build()
                .orElseThrow();

        try {
            assertEquals(DatabaseConfigType.JDBC, database.getConfigData().getConfigType());
            assertFalse(database.jpa().isConfigured());
            assertFalse(database.jpa().isOpen());
            assertThrows(PostgresJpaException.class, () -> database.jpa().read(entityManager -> null));
        } finally {
            database.close();
        }
    }

    @Test
    void storesExplicitJpaEntityAndClassLoaderConfiguration() {
        ClassLoader classLoader = getClass().getClassLoader();
        PostgresConfigData configData = new PostgresConfigData(
                "jdbc:postgresql://localhost/test",
                "user",
                "password",
                false,
                "novus-test",
                java.util.List.of("org.tavall.database.postgres.jpa.fixtures"),
                java.util.List.of(TestJpaEntity.class, TestJpaEntity.class),
                classLoader,
                true,
                true
        );

        assertTrue(configData.isJpaConfigured());
        assertEquals(DatabaseConfigType.JPA, configData.getConfigType());
        assertEquals("novus-test", configData.getPersistenceUnitName());
        assertEquals(java.util.List.of(TestJpaEntity.class), configData.getEntityClasses());
        assertEquals(classLoader, configData.getEntityClassLoader());
        assertTrue(configData.shouldGenerateSchema());
        assertTrue(configData.shouldShowSql());
    }
}
