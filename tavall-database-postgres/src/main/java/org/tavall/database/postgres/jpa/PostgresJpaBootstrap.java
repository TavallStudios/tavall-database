package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.tavall.database.postgres.IPostgresConfigData;
import org.tavall.database.postgres.exception.PostgresDatabaseException;

import java.util.LinkedHashMap;
import java.util.Map;

final class PostgresJpaBootstrap {
    private static final String DEFAULT_PERSISTENCE_UNIT_NAME = "tavall-postgres";

    EntityManagerFactory createEntityManagerFactory(
            IPostgresConfigData configData,
            ClassLoader classLoader
    ) {
        ClassLoader effectiveClassLoader = classLoader == null
                ? PostgresJpaBootstrap.class.getClassLoader()
                : classLoader;
        Thread currentThread = Thread.currentThread();
        ClassLoader originalClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(effectiveClassLoader);

        StandardServiceRegistry serviceRegistry = null;
        try {
            Map<String, Object> settings = settings(configData);
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(settings)
                    .build();
            MetadataSources metadataSources = new MetadataSources(serviceRegistry);
            new JpaManagedTypeScanner()
                    .scan(effectiveClassLoader, configData.getEntityPackages())
                    .forEach(metadataSources::addAnnotatedClass);
            return metadataSources.buildMetadata().buildSessionFactory();
        } catch (RuntimeException exception) {
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }
            throw new PostgresDatabaseException(
                    "Unable to initialize PostgreSQL JPA persistence unit "
                            + persistenceUnitName(configData),
                    exception
            );
        } finally {
            currentThread.setContextClassLoader(originalClassLoader);
        }
    }

    private Map<String, Object> settings(IPostgresConfigData configData) {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("jakarta.persistence.jdbc.url", configData.getJdbcUrl());
        settings.put("jakarta.persistence.jdbc.user", configData.getUsername());
        settings.put("jakarta.persistence.jdbc.password", configData.getPassword());
        settings.put(
                "jakarta.persistence.jdbc.driver",
                resolveDriver(configData.getJdbcUrl())
        );
        settings.put(
                "hibernate.hbm2ddl.auto",
                configData.shouldGenerateSchema() ? "create" : "none"
        );
        settings.put("hibernate.show_sql", configData.shouldShowSql());
        settings.put("hibernate.format_sql", configData.shouldShowSql());
        settings.put("hibernate.session_factory_name", persistenceUnitName(configData));
        settings.put("hibernate.current_session_context_class", "thread");
        return settings;
    }

    private String persistenceUnitName(IPostgresConfigData configData) {
        String configuredName = configData.getPersistenceUnitName();
        if (configuredName == null || configuredName.isBlank()) {
            return DEFAULT_PERSISTENCE_UNIT_NAME;
        }
        return configuredName.trim();
    }

    private String resolveDriver(String jdbcUrl) {
        if (jdbcUrl != null && jdbcUrl.startsWith("jdbc:h2:")) {
            return "org.h2.Driver";
        }
        return "org.postgresql.Driver";
    }
}
