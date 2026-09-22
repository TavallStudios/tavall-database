package org.tavall.database.postgres;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class PostgresQueryHandlerStatementTest {

    @Test
    void executesTrustedSchemaStatementWithoutPreparedParameterParsing() {
        IPostgresDatabase database = PostgresDatabaseBuilder.create()
                .jdbcUrl("jdbc:h2:mem:tavall_statement;DB_CLOSE_DELAY=-1")
                .username("sa")
                .password("")
                .build()
                .orElseThrow();

        try {
            assertTrue(database.queries().executeStatement(
                    "CREATE TABLE statement_probe (id INTEGER PRIMARY KEY)"
            ));
            assertTrue(database.queries().executePreparedStatement(
                    "INSERT INTO statement_probe (id) VALUES (?)",
                    7
            ));
            assertEquals(
                    7,
                    database.queries().queryOne(
                            "SELECT id FROM statement_probe",
                            resultSet -> resultSet.getInt("id")
                    ).orElseThrow()
            );
        } finally {
            database.close();
        }
    }
}
