package org.tavall.database.redis.connection;

import org.junit.jupiter.api.Test;
import org.tavall.database.redis.RedisConfigData;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class RedisConnectionHandlerTlsTest {
    @Test
    void formatsTlsConnectionsWithRedissScheme() {
        RedisConfigData configData = new RedisConfigData(
                "redis.internal",
                6380,
                "user",
                "secret",
                false,
                3,
                true
        );

        assertTrue(RedisConnectionHandler.buildRedisUrl(configData).startsWith("rediss://"));
    }

    @Test
    void preservesLegacyRedisSchemeWhenTlsIsDisabled() {
        RedisConfigData configData = new RedisConfigData(
                "127.0.0.1",
                6379,
                "",
                "",
                false,
                0,
                false
        );

        assertTrue(RedisConnectionHandler.buildRedisUrl(configData).startsWith("redis://"));
    }
}
