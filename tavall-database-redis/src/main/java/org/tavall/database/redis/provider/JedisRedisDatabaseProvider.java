package org.tavall.database.redis.provider;

import org.tavall.database.redis.IRedisDatabaseBuilder;
import org.tavall.database.redis.RedisDatabaseBuilder;

/** ServiceLoader registration for the Jedis-backed Redis provider. */
public final class JedisRedisDatabaseProvider implements IRedisDatabaseProvider {

    public static final String PROVIDER_ID = "jedis";

    @Override
    public String providerId() {
        return PROVIDER_ID;
    }

    @Override
    public IRedisDatabaseBuilder createBuilder() {
        return RedisDatabaseBuilder.create();
    }
}
