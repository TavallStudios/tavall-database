package org.tavall.database.postgres;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.query.IPostgresQueryHandler;

public interface IPostgresDatabase extends IDatabase {

    @Override
    IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> getDatabaseType();

    @Override
    IPostgresConfigData getConfigData();

    IPostgresConnectionHandler connections();

    @Override
    IPostgresQueryHandler queries();
}
