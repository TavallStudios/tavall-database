package org.tavall.database.postgres;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.jpa.PostgresJpaContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostgresJpaBorrowedFactoryTest {

    @Test
    void closingBorrowedContextLeavesFactoryOpen() {
        IPostgresDatabase owner = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_borrowed;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .persistenceUnitName("tavall-borrowed")
                .entityPackage("org.tavall.database.postgres.fixture")
                .generateSchema(true)
                .build()
                .orElseThrow();
        EntityManagerFactory factory = owner.jpa().entityManagerFactory();
        PostgresJpaContext borrowed = PostgresJpaContext.borrowed(factory);

        borrowed.close();

        assertFalse(borrowed.isOpen());
        assertTrue(factory.isOpen());
        assertThrows(
                IllegalStateException.class,
                () -> borrowed.read(entityManager -> null)
        );

        owner.close();
        assertFalse(factory.isOpen());
    }
}
