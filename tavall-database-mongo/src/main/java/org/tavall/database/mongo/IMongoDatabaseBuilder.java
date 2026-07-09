package org.tavall.database.mongo;

import org.tavall.database.core.builder.IHostPortDatabaseBuilder;

public interface IMongoDatabaseBuilder
        extends IHostPortDatabaseBuilder<IMongoDatabase, IMongoDatabaseBuilder> {

    IMongoDatabaseBuilder databaseName(String databaseName);
}

