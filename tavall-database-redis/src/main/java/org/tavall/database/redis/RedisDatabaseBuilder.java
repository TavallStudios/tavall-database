package org.tavall.database.redis;

import org.tavall.database.redis.connection.IRedisConnectionHandler;
import org.tavall.database.redis.connection.RedisConnectionHandler;
import org.tavall.database.redis.exception.RedisDatabaseException;
import org.tavall.database.redis.query.IRedisQueryHandler;
import org.tavall.database.redis.query.RedisQueryHandler;
import org.tavall.logging.Log;

import java.util.Optional;

public final class RedisDatabaseBuilder implements IRedisDatabaseBuilder {

    private String host;
    private int port = 6379;
    private String username;
    private String password;
    private boolean readOnly;
    private int databaseIndex;

    private RedisDatabaseBuilder() {
    }

    public static RedisDatabaseBuilder create() {
        return new RedisDatabaseBuilder();
    }

    @Override
    public RedisDatabaseBuilder host(String host) {
        this.host = host;
        return this;
    }

    @Override
    public RedisDatabaseBuilder port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public RedisDatabaseBuilder username(String username) {
        this.username = username;
        return this;
    }

    @Override
    public RedisDatabaseBuilder password(String password) {
        this.password = password;
        return this;
    }

    @Override
    public RedisDatabaseBuilder readOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }

    @Override
    public RedisDatabaseBuilder databaseIndex(int databaseIndex) {
        this.databaseIndex = databaseIndex;
        return this;
    }

    @Override
    public Optional<IRedisDatabase> build() {
        if (host == null || host.isBlank()) {
            RedisDatabaseException exception = new RedisDatabaseException(
                    "Unable to build Redis database because host is null or blank."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (port <= 0) {
            RedisDatabaseException exception = new RedisDatabaseException(
                    "Unable to build Redis database because port is invalid."
            );
            Log.exception(exception);
            return Optional.empty();
        }
        if (databaseIndex < 0) {
            RedisDatabaseException exception = new RedisDatabaseException(
                    "Unable to build Redis database because databaseIndex is negative."
            );
            Log.exception(exception);
            return Optional.empty();
        }

        try {
            RedisConfigData configData = new RedisConfigData(
                    host,
                    port,
                    username,
                    password,
                    readOnly,
                    databaseIndex
            );
            IRedisConnectionHandler connections = new RedisConnectionHandler(configData);
            IRedisQueryHandler queries = new RedisQueryHandler(connections);
            IRedisDatabase database = new RedisDatabase(configData, connections, queries);
            return Optional.of(database);
        } catch (RuntimeException exception) {
            RedisDatabaseException redisDatabaseException = new RedisDatabaseException(
                    "Unable to build Redis database.",
                    exception
            );
            Log.exception(redisDatabaseException);
            return Optional.empty();
        }
    }
}
