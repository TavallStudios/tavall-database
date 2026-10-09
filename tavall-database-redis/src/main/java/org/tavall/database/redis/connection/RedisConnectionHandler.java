package org.tavall.database.redis.connection;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.tavall.database.redis.IRedisConfigData;
import org.tavall.database.redis.exception.RedisConnectionException;
import org.tavall.logging.Log;
import redis.clients.jedis.JedisPooled;

/** Owns one shared Jedis pool for a configured Redis database. */
public final class RedisConnectionHandler implements IJedisRedisConnectionHandler {

    private final JedisPooled client;
    private volatile boolean closed;

    public RedisConnectionHandler(IRedisConfigData configData) {
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
        // JedisPooled is the handler-owned shared pool. Per-operation callers must not close it.
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
    public synchronized void close() {
        if (closed) {
            return;
        }
        closed = true;
        client.close();
    }

    static String buildRedisUrl(IRedisConfigData configData) {
        StringBuilder redisUrl = new StringBuilder(configData.isTlsEnabled() ? "rediss://" : "redis://");
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
