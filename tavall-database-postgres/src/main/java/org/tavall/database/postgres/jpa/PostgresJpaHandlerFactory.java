package org.tavall.database.postgres.jpa;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.tavall.database.postgres.IPostgresConfigData;
import org.tavall.database.postgres.connection.IPostgresConnectionHandler;
import org.tavall.database.postgres.exception.PostgresJpaException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PostgresJpaHandlerFactory {

    private PostgresJpaHandlerFactory() {
    }

    public static IPostgresJpaHandler create(
            IPostgresConfigData configData,
            IPostgresConnectionHandler connections
    ) {
        if (!configData.isJpaConfigured()) {
            return UnconfiguredPostgresJpaHandler.INSTANCE;
        }

        List<Class<?>> entityClasses = JpaEntityClassResolver.resolve(configData);
        ClassLoader classLoader = configData.getEntityClassLoader();
        ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
        StandardServiceRegistry serviceRegistry = null;
        try {
            Thread.currentThread().setContextClassLoader(classLoader);
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .applySettings(settings(configData, connections))
                    .build();

            MetadataSources metadataSources = new MetadataSources(serviceRegistry);
            entityClasses.forEach(metadataSources::addAnnotatedClass);
            SessionFactory sessionFactory = metadataSources
                    .buildMetadata()
                    .buildSessionFactory();

            StandardServiceRegistry ownedRegistry = serviceRegistry;
            return new PostgresJpaHandler(
                    sessionFactory,
                    () -> StandardServiceRegistryBuilder.destroy(ownedRegistry),
                    configData.isReadOnly()
            );
        } catch (RuntimeException exception) {
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }
            throw new PostgresJpaException("Unable to initialize PostgreSQL JPA.", exception);
        } finally {
            Thread.currentThread().setContextClassLoader(previousClassLoader);
        }
    }

    private static Map<String, Object> settings(
            IPostgresConfigData configData,
            IPostgresConnectionHandler connections
    ) {
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("hibernate.connection.datasource", connections.dataSource());
        settings.put("hibernate.connection.autocommit", false);
        settings.put("hibernate.hbm2ddl.auto", configData.shouldGenerateSchema() ? "update" : "none");
        settings.put("hibernate.show_sql", configData.shouldShowSql());
        settings.put("hibernate.format_sql", configData.shouldShowSql());
        settings.put("hibernate.session_factory_name", configData.getPersistenceUnitName());
        settings.put("hibernate.jdbc.time_zone", "UTC");
        settings.put("hibernate.archive.autodetection", "none");
        return settings;
    }
}
