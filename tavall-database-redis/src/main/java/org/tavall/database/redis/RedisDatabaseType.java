package org.tavall.database.redis;

import org.tavall.database.core.database.IDatabaseType;

public final class RedisDatabaseType implements IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> {

    public static final RedisDatabaseType REDIS = new RedisDatabaseType();

    private RedisDatabaseType() {
    }

    @Override
    public String getTypeId() {
        return "redis";
    }

    @Override
    public IRedisDatabaseBuilder createBuilder() {
        return RedisDatabaseBuilder.create();
    }
}
