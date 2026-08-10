package org.tavall.database.postgres;

import org.tavall.database.core.database.IDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.entity.IPostgresEntityStore;
import org.tavall.database.postgres.jpa.IPostgresJpaContext;
import org.tavall.database.postgres.query.IPostgresQueryHandler;

public interface IPostgresDatabase extends IDatabase {

    @Override
    IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> getDatabaseType();

    @Override
    IPostgresConfigData getConfigData();

    IPostgresConnectionHandler connections();

    /**
     * Entity-oriented persistence boundary for application modules.
     */
    IPostgresEntityStore entities();

    /**
     * Compatibility access for infrastructure migration only.
     * Application modules should declare mapped entities and use
     * {@link #entities()} instead of owning transaction callbacks.
     */
    @Deprecated(forRemoval = false, since = "1.1.0")
    IPostgresJpaContext jpa();

    @Override
    IPostgresQueryHandler queries();
}
