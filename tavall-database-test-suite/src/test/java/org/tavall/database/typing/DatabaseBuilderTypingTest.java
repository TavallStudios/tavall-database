package org.tavall.database.typing;

import org.junit.jupiter.api.Test;
import org.tavall.database.DatabaseType;
import org.tavall.database.core.database.DatabaseBuilder;
import org.tavall.database.mongo.IMongoDatabaseBuilder;
import org.tavall.database.postgres.IPostgresDatabaseBuilder;
import org.tavall.database.qdrant.IQdrantDatabaseBuilder;
import org.tavall.database.redis.IRedisDatabaseBuilder;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertThrows;

final class DatabaseBuilderTypingTest {

    @Test
    void postgresDatabaseTypeNarrowsToJdbcAndJpaBuilderMethods() {
        IPostgresDatabaseBuilder postgresDatabaseBuilder = DatabaseBuilder.create()
                .databaseType(DatabaseType.POSTGRES);

        postgresDatabaseBuilder.jdbcUrl("jdbc:postgresql://localhost:5432/test")
                .username("user")
                .password("password")
                .readOnly(false)
                .persistenceUnitName("main-postgres")
                .entityPackage("com.tavall.player.entity")
                .generateSchema(false)
                .showSql(false);
    }

    @Test
    void redisDatabaseTypeNarrowsToHostPortBuilderMethods() {
        IRedisDatabaseBuilder redisDatabaseBuilder = DatabaseBuilder.create()
                .databaseType(DatabaseType.REDIS);

        redisDatabaseBuilder.host("127.0.0.1")
                .port(6379)
                .username("user")
                .password("password")
                .readOnly(false)
                .databaseIndex(0);
    }

    @Test
    void mongoDatabaseTypeNarrowsToHostPortAndMongoBuilderMethods() {
        IMongoDatabaseBuilder mongoDatabaseBuilder = DatabaseBuilder.create()
                .databaseType(DatabaseType.MONGO);

        mongoDatabaseBuilder.host("127.0.0.1")
                .port(27017)
                .username("user")
                .password("password")
                .readOnly(false)
                .databaseName("tavall");
    }

    @Test
    void qdrantDatabaseTypeNarrowsToHostPortAndQdrantBuilderMethods() {
        IQdrantDatabaseBuilder qdrantDatabaseBuilder = DatabaseBuilder.create()
                .databaseType(DatabaseType.QDRANT);

        qdrantDatabaseBuilder.host("127.0.0.1")
                .port(6333)
                .username("user")
                .password("password")
                .readOnly(false)
                .collectionName("tavall_vectors");
    }

    @Test
    void capabilityMethodsStayOnTheCorrectBuilderInterfaces() {
        assertHasMethod(IPostgresDatabaseBuilder.class, "jdbcUrl", String.class);
        assertHasMethod(IPostgresDatabaseBuilder.class, "persistenceUnitName", String.class);
        assertHasMethod(IPostgresDatabaseBuilder.class, "entityPackage", String.class);
        assertHasMethod(IPostgresDatabaseBuilder.class, "generateSchema", boolean.class);
        assertHasMethod(IPostgresDatabaseBuilder.class, "showSql", boolean.class);

        assertHasMethod(IRedisDatabaseBuilder.class, "host", String.class);
        assertHasMethod(IRedisDatabaseBuilder.class, "port", int.class);
        assertHasMethod(IRedisDatabaseBuilder.class, "databaseIndex", int.class);
        assertDoesNotHaveMethod(IRedisDatabaseBuilder.class, "jdbcUrl", String.class);

        assertHasMethod(IMongoDatabaseBuilder.class, "databaseName", String.class);
        assertDoesNotHaveMethod(IMongoDatabaseBuilder.class, "jdbcUrl", String.class);

        assertHasMethod(IQdrantDatabaseBuilder.class, "collectionName", String.class);
        assertDoesNotHaveMethod(IQdrantDatabaseBuilder.class, "jdbcUrl", String.class);
    }

    private static void assertHasMethod(
            Class<?> type,
            String methodName,
            Class<?>... parameterTypes
    ) {
        try {
            Method method = type.getMethod(methodName, parameterTypes);
            if (method == null) {
                throw new AssertionError("Expected method " + methodName + " on " + type.getName());
            }
        } catch (NoSuchMethodException exception) {
            throw new AssertionError("Expected method " + methodName + " on " + type.getName(), exception);
        }
    }

    private static void assertDoesNotHaveMethod(
            Class<?> type,
            String methodName,
            Class<?>... parameterTypes
    ) {
        assertThrows(NoSuchMethodException.class, () -> type.getMethod(methodName, parameterTypes));
    }
}
