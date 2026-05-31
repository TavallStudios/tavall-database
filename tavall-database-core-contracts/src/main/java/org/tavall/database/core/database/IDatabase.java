package org.tavall.database.core.database;

import org.tavall.database.core.query.IDatabaseQueryHandler;

public interface IDatabase extends AutoCloseable {

    IDatabaseType<?, ?> getDatabaseType();

    IDatabaseConfigData getConfigData();

    IDatabaseQueryHandler queries();

    boolean isAvailable();

    @Override
    void close();
}
