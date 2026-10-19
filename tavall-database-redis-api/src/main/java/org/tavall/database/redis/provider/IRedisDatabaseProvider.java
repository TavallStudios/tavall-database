package org.tavall.database.redis.provider;

import org.tavall.database.redis.IRedisDatabaseBuilder;

/**
 * Runtime-selected Redis provider SPI. A concrete provider module registers one implementation under
 * {@code META-INF/services/org.tavall.database.redis.provider.IRedisDatabaseProvider}, so consumers compile
 * against this API only and the runtime classpath selects the provider.
 */
public interface IRedisDatabaseProvider {

    /** Stable provider identifier, for example {@code jedis}. */
    String providerId();

    IRedisDatabaseBuilder createBuilder();
}
