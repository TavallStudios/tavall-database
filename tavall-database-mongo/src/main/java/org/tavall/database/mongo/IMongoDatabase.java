package org.tavall.database.mongo;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.mongo.connection.IMongoConnectionHandler;
import org.tavall.database.mongo.query.IMongoQueryHandler;

public interface IMongoDatabase extends IDatabase {

    @Override
    IDatabaseType<IMongoDatabase, IMongoDatabaseBuilder> getDatabaseType();

    @Override
    IMongoConfigData getConfigData();

    IMongoConnectionHandler connections();

    @Override
    IMongoQueryHandler queries();
}

