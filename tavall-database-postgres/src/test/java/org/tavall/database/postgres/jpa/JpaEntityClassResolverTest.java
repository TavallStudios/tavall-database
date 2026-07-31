package org.tavall.database.postgres.jpa;

import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.PostgresConfigData;
import org.tavall.database.postgres.exception.PostgresJpaException;
import org.tavall.database.postgres.jpa.fixtures.TestJpaEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JpaEntityClassResolverTest {

    @Test
    void resolvesExplicitAndPackageEntitiesWithoutDuplicates() {
        PostgresConfigData configData = new PostgresConfigData(
                "jdbc:postgresql://localhost/test",
                "",
                "",
                false,
                "test-unit",
                List.of("org.tavall.database.postgres.jpa.fixtures"),
                List.of(TestJpaEntity.class),
                getClass().getClassLoader(),
                false,
                false
        );

        assertEquals(List.of(TestJpaEntity.class), JpaEntityClassResolver.resolve(configData));
    }

    @Test
    void rejectsExplicitClassesThatAreNotJpaManagedTypes() {
        PostgresConfigData configData = new PostgresConfigData(
                "jdbc:postgresql://localhost/test",
                "",
                "",
                false,
                "test-unit",
                List.of(),
                List.of(String.class),
                getClass().getClassLoader(),
                false,
                false
        );

        assertThrows(PostgresJpaException.class, () -> JpaEntityClassResolver.resolve(configData));
    }
}
