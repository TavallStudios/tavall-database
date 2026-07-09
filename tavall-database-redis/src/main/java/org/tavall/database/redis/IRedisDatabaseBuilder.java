package org.tavall.database.redis;

import org.tavall.database.core.builder.IHostPortDatabaseBuilder;

public interface IRedisDatabaseBuilder
        extends IHostPortDatabaseBuilder<IRedisDatabase, IRedisDatabaseBuilder> {

    IRedisDatabaseBuilder databaseIndex(int databaseIndex);
}

