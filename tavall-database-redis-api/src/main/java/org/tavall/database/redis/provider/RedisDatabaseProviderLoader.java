package org.tavall.database.redis.provider;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import org.tavall.database.redis.IRedisDatabaseBuilder;
import org.tavall.database.redis.exception.RedisDatabaseException;

/**
 * Composition-boundary factory that resolves the runtime Redis provider. Use it where the consuming runtime
 * composes its dependencies, then register the built {@code IRedisDatabase} with Tavall DI; ordinary consumers
 * receive {@code IRedisDatabase} through {@code DependencyAccess} rather than calling this loader.
 */
public final class RedisDatabaseProviderLoader {

    private final ClassLoader classLoader;

    public RedisDatabaseProviderLoader(ClassLoader classLoader) {
        this.classLoader = Objects.requireNonNull(classLoader, "classLoader");
    }

    /** Exactly one provider must be present; zero or several fail fast instead of picking arbitrarily. */
    public IRedisDatabaseProvider resolve() {
        List<IRedisDatabaseProvider> providers = ServiceLoader.load(IRedisDatabaseProvider.class, classLoader)
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList();
        if (providers.size() != 1) {
            throw new RedisDatabaseException("Expected exactly one Redis provider on the runtime classpath, found "
                    + providers.stream().map(IRedisDatabaseProvider::providerId).toList());
        }
        return providers.getFirst();
    }

    public IRedisDatabaseBuilder createBuilder() {
        return resolve().createBuilder();
    }
}
