package org.tavall.database.postgres;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseType;

import java.util.List;

public final class PostgresConfigData implements IPostgresConfigData {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final boolean readOnly;
    private final String persistenceUnitName;
    private final List<String> entityPackages;
    private final boolean generateSchema;
    private final boolean showSql;

    public PostgresConfigData(
            String jdbcUrl,
            String username,
            String password,
            boolean readOnly,
            String persistenceUnitName,
            List<String> entityPackages,
            boolean generateSchema,
            boolean showSql
    ) {
        this.jdbcUrl = jdbcUrl;
        this.username = normalizeText(username);
        this.password = normalizeText(password);
        this.readOnly = readOnly;
        this.persistenceUnitName = normalizeText(persistenceUnitName);
        this.entityPackages = entityPackages == null ? List.of() : List.copyOf(entityPackages);
        this.generateSchema = generateSchema;
        this.showSql = showSql;
    }

    @Override
    public IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> getDatabaseType() {
        return PostgresDatabaseType.POSTGRES;
    }

    @Override
    public DatabaseConfigType getConfigType() {
        return DatabaseConfigType.JDBC;
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    @Override
    public List<String> getEntityPackages() {
        return entityPackages;
    }

    @Override
    public boolean shouldGenerateSchema() {
        return generateSchema;
    }

    @Override
    public boolean shouldShowSql() {
        return showSql;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value;
    }
}

