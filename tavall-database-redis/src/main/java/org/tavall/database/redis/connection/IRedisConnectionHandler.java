package org.tavall.database.redis.connection;

import redis.clients.jedis.JedisPooled;

import java.util.Optional;

public interface IRedisConnectionHandler extends AutoCloseable {

    Optional<JedisPooled> openClient();

    void closeClient(JedisPooled client);

    boolean isAvailable();

    @Override
    void close();
}
