package org.tavall.database.postgres;

import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.connection.PostgresConnectionHandler;
import org.tavall.database.postgres.exception.PostgresDatabaseException;
import org.tavall.database.postgres.jpa.IPostgresJpaHandler;
import org.tavall.database.postgres.jpa.PostgresJpaHandlerFactory;
import org.tavall.database.postgres.query.IPostgresQueryHandler;
import org.tavall.database.postgres.query.PostgresQueryHandler;
import org.tavall.logging.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public final class PostgresDatabaseBuilder implements IPostgresDatabaseBuilder {

    private String jdbcUrl;
    private String username;
    private String password;
    private boolean readOnly;
    private String persistenceUnitName;
    private final List<String> entityPackages;
    private final List<Class<?>> entityClasses;
    private ClassLoader entityClassLoader;
    private boolean generateSchema;
    private boolean showSql;

    private PostgresDatabaseBuilder() {
        this.entityPackages = new ArrayList<>();
        this.entityClasses = new ArrayList<>();
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
    public PostgresDatabaseBuilder entityClass(Class<?> entityClass) {
        if (entityClass != null) {
            entityClasses.add(entityClass);
        }
        return this;
    }

    @Override
    public PostgresDatabaseBuilder entityClasses(Class<?>... entityClasses) {
        if (entityClasses != null) {
            Arrays.stream(entityClasses)
                    .filter(entityClass -> entityClass != null)
                    .forEach(this.entityClasses::add);
        }
        return this;
    }

    @Override
    public PostgresDatabaseBuilder entityClassLoader(ClassLoader entityClassLoader) {
        this.entityClassLoader = entityClassLoader;
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

        IPostgresConnectionHandler connections = null;
        IPostgresJpaHandler jpa = null;
        try {
            PostgresConfigData configData = new PostgresConfigData(
                    jdbcUrl,
                    username,
                    password,
                    readOnly,
                    persistenceUnitName,
                    entityPackages,
                    entityClasses,
                    entityClassLoader,
                    generateSchema,
                    showSql
            );
            connections = new PostgresConnectionHandler(configData);
            IPostgresQueryHandler queries = new PostgresQueryHandler(connections);
            jpa = PostgresJpaHandlerFactory.create(configData, connections);
            IPostgresDatabase database = new PostgresDatabase(configData, connections, jpa, queries);
            return Optional.of(database);
        } catch (RuntimeException exception) {
            closeAfterFailure(jpa, connections, exception);
            PostgresDatabaseException postgresDatabaseException = new PostgresDatabaseException(
                    "Unable to build PostgreSQL database.",
                    exception
            );
            Log.exception(postgresDatabaseException);
            return Optional.empty();
        }
    }

    private void closeAfterFailure(
            IPostgresJpaHandler jpa,
            IPostgresConnectionHandler connections,
            RuntimeException buildFailure
    ) {
        if (jpa != null) {
            try {
                jpa.close();
            } catch (RuntimeException closeFailure) {
                buildFailure.addSuppressed(closeFailure);
            }
        }
        if (connections != null) {
            try {
                connections.close();
            } catch (RuntimeException closeFailure) {
                buildFailure.addSuppressed(closeFailure);
            }
        }
    }
}
