package org.tavall.database.postgres;

import org.tavall.database.core.database.DatabaseConfigType;
import org.tavall.database.core.database.IDatabaseType;

import java.util.LinkedHashSet;
import java.util.List;

public final class PostgresConfigData implements IPostgresConfigData {

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final boolean readOnly;
    private final String persistenceUnitName;
    private final List<String> entityPackages;
    private final List<Class<?>> entityClasses;
    private final ClassLoader entityClassLoader;
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
        this(
                jdbcUrl,
                username,
                password,
                readOnly,
                persistenceUnitName,
                entityPackages,
                List.of(),
                resolveDefaultClassLoader(),
                generateSchema,
                showSql
        );
    }

    public PostgresConfigData(
            String jdbcUrl,
            String username,
            String password,
            boolean readOnly,
            String persistenceUnitName,
            List<String> entityPackages,
            List<Class<?>> entityClasses,
            ClassLoader entityClassLoader,
            boolean generateSchema,
            boolean showSql
    ) {
        this.jdbcUrl = jdbcUrl;
        this.username = normalizeText(username);
        this.password = normalizeText(password);
        this.readOnly = readOnly;
        this.persistenceUnitName = normalizePersistenceUnitName(persistenceUnitName);
        this.entityPackages = normalizeEntityPackages(entityPackages);
        this.entityClasses = normalizeEntityClasses(entityClasses);
        this.entityClassLoader = entityClassLoader == null ? resolveDefaultClassLoader() : entityClassLoader;
        this.generateSchema = generateSchema;
        this.showSql = showSql;
    }

    @Override
    public IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> getDatabaseType() {
        return PostgresDatabaseType.POSTGRES;
    }

    @Override
    public DatabaseConfigType getConfigType() {
        return isJpaConfigured() ? DatabaseConfigType.JPA : DatabaseConfigType.JDBC;
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
    public List<Class<?>> getEntityClasses() {
        return entityClasses;
    }

    @Override
    public ClassLoader getEntityClassLoader() {
        return entityClassLoader;
    }

    @Override
    public boolean isJpaConfigured() {
        return !entityPackages.isEmpty() || !entityClasses.isEmpty();
    }

    @Override
    public boolean shouldGenerateSchema() {
        return generateSchema;
    }

    @Override
    public boolean shouldShowSql() {
        return showSql;
    }

    private static List<String> normalizeEntityPackages(List<String> entityPackages) {
        if (entityPackages == null || entityPackages.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<String> normalizedPackages = new LinkedHashSet<>();
        for (String entityPackage : entityPackages) {
            if (entityPackage != null && !entityPackage.isBlank()) {
                normalizedPackages.add(entityPackage.trim());
            }
        }
        return List.copyOf(normalizedPackages);
    }

    private static List<Class<?>> normalizeEntityClasses(List<Class<?>> entityClasses) {
        if (entityClasses == null || entityClasses.isEmpty()) {
            return List.of();
        }

        LinkedHashSet<Class<?>> normalizedClasses = new LinkedHashSet<>();
        for (Class<?> entityClass : entityClasses) {
            if (entityClass != null) {
                normalizedClasses.add(entityClass);
            }
        }
        return List.copyOf(normalizedClasses);
    }

    private static String normalizePersistenceUnitName(String persistenceUnitName) {
        if (persistenceUnitName == null || persistenceUnitName.isBlank()) {
            return "tavall-postgres";
        }
        return persistenceUnitName.trim();
    }

    private static String normalizeText(String value) {
        return value == null ? "" : value;
    }

    private static ClassLoader resolveDefaultClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
            return contextClassLoader;
        }

        ClassLoader ownClassLoader = PostgresConfigData.class.getClassLoader();
        if (ownClassLoader != null) {
            return ownClassLoader;
        }

        return ClassLoader.getSystemClassLoader();
    }
}
