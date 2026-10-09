package org.tavall.database.redis.connection;

/**
 * Provider-neutral Redis connection lifecycle.
 *
 * <p>The API intentionally exposes no concrete client type. Consumers that still need raw client access
 * depend on the concrete provider module and its provider-specific subtype; typed operations are reached
 * through {@code IRedisDatabase} capabilities instead.</p>
 */
public interface IRedisConnectionHandler extends AutoCloseable {

    /** Returns {@code true} when the provider can currently reach Redis. Never throws for connectivity loss. */
    boolean isAvailable();

    /** Releases provider-owned pools. Idempotent; later operations fail with {@code RedisConnectionException}. */
    @Override
    void close();
}
