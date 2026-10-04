package org.tavall.database.postgres.entity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

final class PostgresEntityOperationContextTest {
    private static final String EXPECTED_QUERY =
            "SELECT pg_advisory_xact_lock(" +
                    "hashtextextended(CAST(:lockKey AS text), 0))";

    @Test
    void advisoryLockUsesTheActiveAtomicContextAndBindsTheStableKey() {
        AtomicReference<String> capturedQuery = new AtomicReference<>();
        AtomicReference<String> capturedLockKey = new AtomicReference<>();
        AtomicInteger executionCount = new AtomicInteger();

        Query query = (Query) Proxy.newProxyInstance(
                Query.class.getClassLoader(),
                new Class<?>[]{Query.class},
                (proxy, method, arguments) -> {
                    return switch (method.getName()) {
                        case "setParameter" -> {
                            assertEquals("lockKey", arguments[0]);
                            capturedLockKey.set((String) arguments[1]);
                            yield proxy;
                        }
                        case "getSingleResult" -> {
                            executionCount.incrementAndGet();
                            yield 1L;
                        }
                        default -> throw new UnsupportedOperationException(
                                method.getName()
                        );
                    };
                }
        );
        EntityManager entityManager = (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class<?>[]{EntityManager.class},
                (proxy, method, arguments) -> {
                    if (!method.getName().equals("createNativeQuery")) {
                        throw new UnsupportedOperationException(
                                method.getName()
                        );
                    }
                    capturedQuery.set((String) arguments[0]);
                    return query;
                }
        );
        PostgresEntityOperationContext context =
                new PostgresEntityOperationContext(entityManager);

        context.acquireAdvisoryTransactionLock("provider-external:discord:1234");

        assertEquals(EXPECTED_QUERY, capturedQuery.get());
        assertEquals("provider-external:discord:1234", capturedLockKey.get());
        assertEquals(1, executionCount.get());

        context.invalidate();
        assertThrows(
                IllegalStateException.class,
                () -> context.acquireAdvisoryTransactionLock("another-key")
        );
        assertEquals(1, executionCount.get());
    }

    @Test
    void advisoryLockRejectsBlankKeysBeforeQueryExecution() {
        AtomicInteger executionCount = new AtomicInteger();
        Query query = (Query) Proxy.newProxyInstance(
                Query.class.getClassLoader(),
                new Class<?>[]{Query.class},
                (proxy, method, arguments) -> {
                    if (method.getName().equals("getSingleResult")) {
                        executionCount.incrementAndGet();
                        return 1L;
                    }
                    return proxy;
                }
        );
        EntityManager entityManager = (EntityManager) Proxy.newProxyInstance(
                EntityManager.class.getClassLoader(),
                new Class<?>[]{EntityManager.class},
                (proxy, method, arguments) -> query
        );
        PostgresEntityOperationContext context =
                new PostgresEntityOperationContext(entityManager);

        assertThrows(
                IllegalArgumentException.class,
                () -> context.acquireAdvisoryTransactionLock("  ")
        );
        assertEquals(0, executionCount.get());
    }
}
