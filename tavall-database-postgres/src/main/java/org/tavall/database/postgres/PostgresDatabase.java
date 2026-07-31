package org.tavall.database.postgres;

import org.tavall.database.core.database.AbstractDatabase;
import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.jpa.IPostgresJpaHandler;
import org.tavall.database.postgres.jpa.PostgresJpaHandlerFactory;
import org.tavall.database.postgres.query.IPostgresQueryHandler;

import java.util.Objects;

public final class PostgresDatabase extends AbstractDatabase<IPostgresConfigData> implements IPostgresDatabase {

    private final IPostgresConnectionHandler connections;
    private final IPostgresJpaHandler jpa;
    private final IPostgresQueryHandler queries;

    public PostgresDatabase(
            IPostgresConfigData configData,
            IPostgresConnectionHandler connections,
            IPostgresQueryHandler queries
    ) {
        this(configData, connections, PostgresJpaHandlerFactory.unconfigured(), queries);
    }

    public PostgresDatabase(
            IPostgresConfigData configData,
            IPostgresConnectionHandler connections,
            IPostgresJpaHandler jpa,
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
    public IPostgresJpaHandler jpa() {
        return jpa;
    }

    @Override
    public IPostgresQueryHandler queries() {
        return queries;
    }

    @Override
    public boolean isAvailable() {
        return connections.isAvailable() && (!jpa.isConfigured() || jpa.isOpen());
    }

    @Override
    public void close() {
        RuntimeException failure = null;
        try {
            jpa.close();
        } catch (RuntimeException jpaFailure) {
            failure = jpaFailure;
        }

        try {
            connections.close();
        } catch (RuntimeException connectionFailure) {
            if (failure == null) {
                failure = connectionFailure;
            } else {
                failure.addSuppressed(connectionFailure);
            }
        }

        if (failure != null) {
            throw failure;
        }
    }
}
