package org.tavall.database.redis;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.tavall.database.DatabaseType;
import org.tavall.database.core.database.DatabaseBuilder;
import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.testing.RemoteRedisDatabaseConfig;
import redis.clients.jedis.JedisPooled;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RedisDatabaseSmokeTest {

    @Test
    void connectsToRemoteRedisAndRoundTripsAValue() {
        RemoteRedisDatabaseConfig config = RemoteRedisDatabaseConfig.fromEnv();

        Optional<IRedisDatabase> databaseOptional = DatabaseBuilder.create()
                .databaseType(DatabaseType.REDIS)
                .host(config.host())
                .port(config.port())
                .username(config.username())
                .password(config.password())
                .readOnly(config.readOnly())
                .databaseIndex(config.databaseIndex())
                .build();

        assertTrue(databaseOptional.isPresent());

        IRedisDatabase database = databaseOptional.get();
        JedisPooled client = null;
        try {
            IRedisConfigData configData = database.getConfigData();

            assertEquals(DatabaseType.REDIS, database.getDatabaseType());
            assertEquals(DatabaseType.REDIS, configData.getDatabaseType());
            assertEquals(DatabaseConfigType.HOST_PORT, configData.getConfigType());
            assertEquals(config.host(), configData.getHost());
            assertEquals(config.port(), configData.getPort());
            assertEquals(config.username(), configData.getUsername());
            assertEquals(config.password(), configData.getPassword());
            assertEquals(config.readOnly(), configData.isReadOnly());
            assertEquals(config.databaseIndex(), configData.getDatabaseIndex());

            Assumptions.assumeTrue(
                    database.isAvailable(),
                    "Remote Redis database is not reachable."
            );

            client = database.connections().openClient().orElseThrow();
            String key = config.key();
            String value = config.value();

            client.del(key);

            String setResult = client.set(key, value);
            assertEquals("OK", setResult);
            assertEquals(value, client.get(key));
            assertEquals(1L, client.del(key));
        } finally {
            if (client != null) {
                client.del(config.key());
            }
            database.close();
        }
    }
}
