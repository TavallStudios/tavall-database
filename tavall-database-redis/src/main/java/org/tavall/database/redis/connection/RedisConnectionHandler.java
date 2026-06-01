package org.tavall.database.redis.connection;

import org.tavall.database.redis.IRedisConfigData;
import org.tavall.database.redis.exception.RedisConnectionException;
import org.tavall.logging.Log;
import redis.clients.jedis.JedisPooled;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class RedisConnectionHandler implements IRedisConnectionHandler {

    private final IRedisConfigData configData;
    private final JedisPooled client;
    private boolean closed;

    public RedisConnectionHandler(IRedisConfigData configData) {
        this.configData = configData;
        this.client = new JedisPooled(buildRedisUrl(configData));
    }

    @Override
    public Optional<JedisPooled> openClient() {
        if (closed) {
            return Optional.empty();
        }
        return Optional.of(client);
    }

    @Override
    public void closeClient(JedisPooled client) {
        if (client == null) {
            return;
        }

        // JedisPooled is the handler-owned shared pool. Per-operation callers should not close it.
    }

    @Override
    public boolean isAvailable() {
        if (closed) {
            return false;
        }

        try {
            return "PONG".equalsIgnoreCase(client.ping());
        } catch (RuntimeException exception) {
            RedisConnectionException redisConnectionException = new RedisConnectionException(
                    "Unable to validate Redis connection.",
                    exception
            );
            Log.exception(redisConnectionException);
            return false;
        }
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        closeClient(client);
    }

    private String buildRedisUrl(IRedisConfigData configData) {
        StringBuilder redisUrl = new StringBuilder("redis://");
        String username = configData.getUsername();
        String password = configData.getPassword();
        boolean hasUsername = username != null && !username.isBlank();
        boolean hasPassword = password != null && !password.isBlank();

        if (hasUsername || hasPassword) {
            if (hasUsername) {
                redisUrl.append(URLEncoder.encode(username, StandardCharsets.UTF_8));
            }
            if (hasPassword) {
                redisUrl.append(':').append(URLEncoder.encode(password, StandardCharsets.UTF_8));
            } else if (hasUsername) {
                redisUrl.append(':');
            }
            redisUrl.append('@');
        }

        redisUrl.append(configData.getHost())
                .append(':')
                .append(configData.getPort())
                .append('/')
                .append(configData.getDatabaseIndex());
        return redisUrl.toString();
    }
}
