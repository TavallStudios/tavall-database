package org.tavall.database.testing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record RemotePostgresDatabaseConfig(
        String jdbcUrl,
        String username,
        String password,
        boolean readOnly,
        String persistenceUnitName,
        List<String> entityPackages,
        boolean generateSchema,
        boolean showSql,
        String tableName,
        UUID playerId,
        String playerUsername,
        Instant createdAt
) {

    public RemotePostgresDatabaseConfig {
        entityPackages = List.copyOf(entityPackages);
    }

    public static RemotePostgresDatabaseConfig fromEnv() {
        String jdbcUrl = RemoteDatabaseEnvironment.requiredValue(
                "Remote PostgreSQL JDBC URL",
                "TAVALL_POSTGRES_URL",
                "NOVUS_POSTGRES_URL",
                "POSTGRES_URL"
        );
        String username = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_POSTGRES_USER",
                "NOVUS_POSTGRES_USER",
                "POSTGRES_USER"
        ).orElse("");
        String password = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_POSTGRES_PASSWORD",
                "NOVUS_POSTGRES_PASSWORD",
                "NOVUS_POSTGRES_PASS",
                "POSTGRES_PASSWORD"
        ).orElse("");
        boolean readOnly = RemoteDatabaseEnvironment.booleanOrDefault(
                false,
                "TAVALL_POSTGRES_READ_ONLY",
                "POSTGRES_READ_ONLY"
        );
        String persistenceUnitName = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_POSTGRES_PERSISTENCE_UNIT_NAME",
                "POSTGRES_PERSISTENCE_UNIT_NAME"
        ).orElse("remote-postgres");
        boolean generateSchema = RemoteDatabaseEnvironment.booleanOrDefault(
                false,
                "TAVALL_POSTGRES_GENERATE_SCHEMA",
                "POSTGRES_GENERATE_SCHEMA"
        );
        boolean showSql = RemoteDatabaseEnvironment.booleanOrDefault(
                false,
                "TAVALL_POSTGRES_SHOW_SQL",
                "POSTGRES_SHOW_SQL"
        );
        String tableName = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_POSTGRES_TEST_TABLE",
                "POSTGRES_TEST_TABLE"
        ).orElse("tavall_remote_postgres_player_data");
        UUID playerId = UUID.fromString(
                RemoteDatabaseEnvironment.firstValue(
                        "TAVALL_POSTGRES_TEST_PLAYER_ID",
                        "POSTGRES_TEST_PLAYER_ID"
                ).orElse("00000000-0000-0000-0000-000000000001")
        );
        String playerUsername = RemoteDatabaseEnvironment.firstValue(
                "TAVALL_POSTGRES_TEST_USERNAME",
                "POSTGRES_TEST_USERNAME"
        ).orElse("remote-player");
        Instant createdAt = Instant.parse(
                RemoteDatabaseEnvironment.firstValue(
                        "TAVALL_POSTGRES_TEST_CREATED_AT",
                        "POSTGRES_TEST_CREATED_AT"
                ).orElse("2024-01-01T00:00:00Z")
        );

        return new RemotePostgresDatabaseConfig(
                jdbcUrl,
                username,
                password,
                readOnly,
                persistenceUnitName,
                List.of("com.tavall.player.entity", "com.tavall.guild.entity"),
                generateSchema,
                showSql,
                tableName,
                playerId,
                playerUsername,
                createdAt
        );
    }
}
