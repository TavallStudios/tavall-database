package org.tavall.database;

import org.tavall.database.core.database.IDatabaseType;
import org.tavall.database.mongo.IMongoDatabase;
import org.tavall.database.mongo.IMongoDatabaseBuilder;
import org.tavall.database.mongo.MongoDatabaseType;
import org.tavall.database.postgres.IPostgresDatabase;
import org.tavall.database.postgres.IPostgresDatabaseBuilder;
import org.tavall.database.postgres.PostgresDatabaseType;
import org.tavall.database.qdrant.IQdrantDatabase;
import org.tavall.database.qdrant.IQdrantDatabaseBuilder;
import org.tavall.database.qdrant.QdrantDatabaseType;
import org.tavall.database.redis.IRedisDatabase;
import org.tavall.database.redis.IRedisDatabaseBuilder;
import org.tavall.database.redis.RedisDatabaseType;

public final class DatabaseType {

    public static final IDatabaseType<IPostgresDatabase, IPostgresDatabaseBuilder> POSTGRES =
            PostgresDatabaseType.POSTGRES;

    public static final IDatabaseType<IRedisDatabase, IRedisDatabaseBuilder> REDIS =
            RedisDatabaseType.REDIS;

    public static final IDatabaseType<IMongoDatabase, IMongoDatabaseBuilder> MONGO =
            MongoDatabaseType.MONGO;

    public static final IDatabaseType<IQdrantDatabase, IQdrantDatabaseBuilder> QDRANT =
            QdrantDatabaseType.QDRANT;

    private DatabaseType() {
    }
}
