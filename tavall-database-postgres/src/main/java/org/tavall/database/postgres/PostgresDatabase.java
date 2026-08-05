package org.tavall.database.postgres;

import org.tavall.database.core.database.AbstractDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.jpa.IPostgresJpaContext;
import org.tavall.database.postgres.query.IPostgresQueryHandler;

import java.util.Objects;

public final class PostgresDatabase extends AbstractDatabase<IPostgresConfigData>
        implements IPostgresDatabase {

    private final IPostgresConnectionHandler connections;
    private final IPostgresJpaContext jpa;
    private final IPostgresQueryHandler queries;

    public PostgresDatabase(
            IPostgresConfigData configData,
            IPostgresConnectionHandler connections,
            IPostgresJpaContext jpa,
            IPostgresQueryHandler queries
    ) {
        super(PostgresDatabaseType.POSTGRES, configData, queries);
        this.connections = Objects.requireNonNull(connections, "connections");
        this.jpa = Objects.requireNonNull(jpa, "jpa");
        this.queries = Objects.requireNonNull(queries, "queries");
    }

    @Override
    public IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> getDatabaseType() {
        return PostgresDatabaseType.POSTGRES;
    }

    @Override
    public IPostgresConfigData getConfigData() {
        return super.getConfigData();
    }

    @Override
    public IPostgresConnectionHandler connections() {
        return connections;
    }

    @Override
    public IPostgresJpaContext jpa() {
        return jpa;
    }

    @Override
    public IPostgresQueryHandler queries() {
        return queries;
    }

    @Override
    public boolean isAvailable() {
        return connections.isAvailable();
    }

    @Override
    public void close() {
        RuntimeException failure = null;
        try {
            jpa.close();
        } catch (RuntimeException exception) {
            failure = exception;
        }
        try {
            connections.close();
        } catch (RuntimeException exception) {
            if (failure == null) {
                failure = exception;
            } else {
                failure.addSuppressed(exception);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }
}
