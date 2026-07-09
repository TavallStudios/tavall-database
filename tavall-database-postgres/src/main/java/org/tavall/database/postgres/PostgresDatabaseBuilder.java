package org.tavall.database.postgres;

import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.connection.PostgresConnectionHandler;
import org.tavall.database.postgres.exception.PostgresDatabaseException;
import org.tavall.database.postgres.query.IPostgresQueryHandler;
import org.tavall.database.postgres.query.PostgresQueryHandler;
import org.tavall.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PostgresDatabaseBuilder implements IPostgresDatabaseBuilder {

    private String jdbcUrl;
    private String username;
    private String password;
    private boolean readOnly;
    private String persistenceUnitName;
    private final List<String> entityPackages;
    private boolean generateSchema;
    private boolean showSql;

    private PostgresDatabaseBuilder() {
        this.entityPackages = new ArrayList<>();
    }

    public static PostgresDatabaseBuilder create() {
        return new PostgresDatabaseBuilder();
    }

    @Override
    public PostgresDatabaseBuilder jdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
        return this;
    }

    @Override
    public PostgresDatabaseBuilder username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public PostgresDatabaseBuilder password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public PostgresDatabaseBuilder readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public PostgresDatabaseBuilder persistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
        return this;
    }

    @Override
    public PostgresDatabaseBuilder entityPackage(String entityPackage) {
        if (entityPackage != null && !entityPackage.isBlank()) {
            entityPackages.add(entityPackage);
        }
        return this;
    }

    @Override
    public PostgresDatabaseBuilder generateSchema(boolean generateSchema) {
        this.generateSchema = generateSchema;
        return this;
    }

    @Override
    public PostgresDatabaseBuilder showSql(boolean showSql) {
        this.showSql = showSql;
        return this;
    }

    @Override
    public Optional<IPostgresDatabase> build() {
        if (jdbcUrl == null || jdbcUrl.isBlank()) {
            PostgresDatabaseException exception = new PostgresDatabaseException(
                    "Unable to build PostgreSQL database because jdbcUrl is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }

        try {
            PostgresConfigData configData = new PostgresConfigData(
                    jdbcUrl,
                    username,
                    password,
                    readOnly,
                    persistenceUnitName,
                    entityPackages,
                    generateSchema,
                    showSql
            );
            IPostgresConnectionHandler connections = new PostgresConnectionHandler(configData);
            IPostgresQueryHandler queries = new PostgresQueryHandler(connections);
            IPostgresDatabase database = new PostgresDatabase(configData, connections, queries);
            return Optional.of(database);
        } catch (RuntimeException exception) {
            PostgresDatabaseException postgresDatabaseException = new PostgresDatabaseException(
                    "Unable to build PostgreSQL database.",
                    exception
            );
            Log.exception(postgresDatabaseException);
            return Optional.empty();
        }
    }

}

