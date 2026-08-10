package org.tavall.database.redis;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RedisConfigDataTlsTest {
    @Test
    void keepsLegacyConfigurationPlaintextByDefault() {
        RedisConfigData configData = new RedisConfigData(
                "127.0.0.1",
                6379,
                "user",
                "secret",
                false,
                0
        );

        assertFalse(configData.isTlsEnabled());
    }

    @Test
    void carriesExplicitTlsConfiguration() {
        RedisConfigData configData = new RedisConfigData(
                "redis.internal",
                6380,
                "user",
                "secret",
                false,
                0,
                true
        );

        assertTrue(configData.isTlsEnabled());
    }
}
