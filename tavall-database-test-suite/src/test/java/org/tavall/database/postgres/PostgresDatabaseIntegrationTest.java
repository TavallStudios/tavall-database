package org.tavall.database.postgres;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.tavall.database.DatabaseType;
import org.tavall.database.core.database.DatabaseBuilder;
import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.postgres.IPostgresDatabaseBuilder;
import org.tavall.database.testing.RemotePostgresDatabaseConfig;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresDatabaseIntegrationTest {

    @Test
    void connectsToRemotePostgresAndRunsQueries() {
        RemotePostgresDatabaseConfig config = RemotePostgresDatabaseConfig.fromEnv();

        IPostgresDatabaseBuilder databaseBuilder = DatabaseBuilder.create()
                .databaseType(DatabaseType.POSTGRES)
                .jdbcUrl(config.jdbcUrl())
                .username(config.username())
                .password(config.password())
                .readOnly(config.readOnly())
                .persistenceUnitName(config.persistenceUnitName())
                .generateSchema(config.generateSchema())
                .showSql(config.showSql());

        for (String entityPackage : config.entityPackages()) {
            databaseBuilder.entityPackage(entityPackage);
        }

        Optional<IPostgresDatabase> databaseOptional = databaseBuilder.build();

        assertTrue(databaseOptional.isPresent());

        IPostgresDatabase database = databaseOptional.get();
        boolean remoteAvailable = false;
        String tableName = config.tableName();
        try {
            IPostgresConfigData configData = database.getConfigData();

            assertEquals(DatabaseType.POSTGRES, database.getDatabaseType());
            assertEquals(DatabaseType.POSTGRES, configData.getDatabaseType());
            assertEquals(DatabaseConfigType.JDBC, configData.getConfigType());
            assertEquals(config.jdbcUrl(), configData.getJdbcUrl());
            assertEquals(config.username(), configData.getUsername());
            assertEquals(config.password(), configData.getPassword());
            assertEquals(config.readOnly(), configData.isReadOnly());
            assertEquals(config.persistenceUnitName(), configData.getPersistenceUnitName());
            assertEquals(config.entityPackages(), configData.getEntityPackages());
            assertEquals(config.generateSchema(), configData.shouldGenerateSchema());
            assertEquals(config.showSql(), configData.shouldShowSql());

            remoteAvailable = database.isAvailable();
            Assumptions.assumeTrue(remoteAvailable, "Remote PostgreSQL database is not reachable.");

            database.queries().executePreparedStatement("DROP TABLE IF EXISTS " + tableName);

            boolean tableCreated = database.queries().executePreparedStatement("""
                    CREATE TABLE IF NOT EXISTS %s (
                        player_id uuid PRIMARY KEY,
                        username text NOT NULL,
                        created_at timestamptz NOT NULL
                    )
                    """.formatted(tableName));
            assertTrue(tableCreated);

            int savedRowCount = database.queries().executePreparedStatementAndCount("""
                    INSERT INTO %s (
                        player_id,
                        username,
                        created_at
                    )
                    VALUES (?, ?, ?)
                    ON CONFLICT (player_id)
                    DO UPDATE SET
                        username = EXCLUDED.username
                    """.formatted(tableName),
                    config.playerId(),
                    config.playerUsername(),
                    config.createdAt()
            );
            assertEquals(1, savedRowCount);

            Optional<PlayerData> playerDataOptional = database.queries().queryOne("""
                    SELECT player_id, username, created_at
                    FROM %s
                    WHERE player_id = ?
                    """.formatted(tableName),
                    resultSet -> new PlayerData(
                            resultSet.getObject("player_id", java.util.UUID.class),
                            resultSet.getString("username"),
                            resultSet.getObject("created_at", java.time.Instant.class)
                    ),
                    config.playerId()
            );

            assertTrue(playerDataOptional.isPresent());
            PlayerData playerData = playerDataOptional.get();
            assertEquals(config.playerId(), playerData.playerId());
            assertEquals(config.playerUsername(), playerData.username());
            assertEquals(config.createdAt(), playerData.createdAt());

            assertEquals(1, database.queries().queryList("""
                    SELECT player_id, username, created_at
                    FROM %s
                    WHERE player_id = ?
                    """.formatted(tableName),
                    resultSet -> new PlayerData(
                            resultSet.getObject("player_id", java.util.UUID.class),
                            resultSet.getString("username"),
                            resultSet.getObject("created_at", java.time.Instant.class)
                    ),
                    config.playerId()
            ).size());
        } finally {
            if (remoteAvailable) {
                database.queries().executePreparedStatement("DROP TABLE IF EXISTS " + tableName);
            }
            database.close();
        }
    }
}

