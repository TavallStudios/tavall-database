package org.tavall.database.core.database;

import org.tavall.database.core.query.IDatabaseQueryHandler;

import java.util.Objects;

public abstract class AbstractDatabase<C extends IDatabaseConfigData> implements IDatabase {

    private final IDatabaseType<?, ?> databaseType;
    private final C configData;
    private final IDatabaseQueryHandler queries;

    protected AbstractDatabase(IDatabaseType<?, ?> databaseType, C configData, IDatabaseQueryHandler queries) {
        this.databaseType = Objects.requireNonNull(databaseType, "databaseType");
        this.configData = Objects.requireNonNull(configData, "configData");
        this.queries = Objects.requireNonNull(queries, "queries");
    }

    @Override
    public IDatabaseType<?, ?> getDatabaseType() {
        return databaseType;
    }

    @Override
    public C getConfigData() {
        return configData;
    }

    @Override
    public IDatabaseQueryHandler queries() {
        return queries;
    }

    @Override
    public void close() {
    }
}

