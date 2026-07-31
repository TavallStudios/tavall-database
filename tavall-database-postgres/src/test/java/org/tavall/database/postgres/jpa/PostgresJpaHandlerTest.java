package org.tavall.database.postgres.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Test;
import org.tavall.database.postgres.exception.PostgresJpaException;

import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresJpaHandlerTest {

    @Test
    void writeOwnsFlushCommitAndEntityManagerCleanup() {
        FakeJpa state = new FakeJpa();
        PostgresJpaHandler handler = new PostgresJpaHandler(
                state.entityManagerFactory(),
                state.providerCloseCalls::incrementAndGet,
                false
        );

        String result = handler.write(entityManager -> "written");

        assertEquals("written", result);
        assertEquals(1, state.createEntityManagerCalls.get());
        assertEquals(1, state.beginCalls.get());
        assertEquals(1, state.flushCalls.get());
        assertEquals(1, state.commitCalls.get());
        assertEquals(0, state.rollbackCalls.get());
        assertEquals(1, state.entityManagerCloseCalls.get());
    }

    @Test
    void readCommitsWithoutFlushing() {
        FakeJpa state = new FakeJpa();
        PostgresJpaHandler handler = new PostgresJpaHandler(
                state.entityManagerFactory(),
                state.providerCloseCalls::incrementAndGet,
                false
        );

        int result = handler.read(entityManager -> 42);

        assertEquals(42, result);
        assertEquals(1, state.beginCalls.get());
        assertEquals(0, state.flushCalls.get());
        assertEquals(1, state.commitCalls.get());
        assertEquals(1, state.entityManagerCloseCalls.get());
    }

    @Test
    void userFailureRollsBackAndPreservesTheOriginalException() {
        FakeJpa state = new FakeJpa();
        PostgresJpaHandler handler = new PostgresJpaHandler(
                state.entityManagerFactory(),
                state.providerCloseCalls::incrementAndGet,
                false
        );
        IllegalArgumentException expected = new IllegalArgumentException("domain failure");

        IllegalArgumentException actual = assertThrows(
                IllegalArgumentException.class,
                () -> handler.write(entityManager -> {
                    throw expected;
                })
        );

        assertSame(expected, actual);
        assertEquals(1, state.rollbackCalls.get());
        assertEquals(0, state.commitCalls.get());
        assertEquals(1, state.entityManagerCloseCalls.get());
    }

    @Test
    void readOnlyHandlerRejectsWritesBeforeOpeningAnEntityManager() {
        FakeJpa state = new FakeJpa();
        PostgresJpaHandler handler = new PostgresJpaHandler(
                state.entityManagerFactory(),
                state.providerCloseCalls::incrementAndGet,
                true
        );

        assertThrows(PostgresJpaException.class, () -> handler.write(entityManager -> null));
        assertEquals(0, state.createEntityManagerCalls.get());
    }

    @Test
    void closeIsIdempotentAndClosesProviderResources() {
        FakeJpa state = new FakeJpa();
        PostgresJpaHandler handler = new PostgresJpaHandler(
                state.entityManagerFactory(),
                state.providerCloseCalls::incrementAndGet,
                false
        );

        assertTrue(handler.isOpen());
        handler.close();
        handler.close();

        assertFalse(handler.isOpen());
        assertEquals(1, state.entityManagerFactoryCloseCalls.get());
        assertEquals(1, state.providerCloseCalls.get());
        assertThrows(PostgresJpaException.class, () -> handler.read(entityManager -> null));
    }

    private static final class FakeJpa {
        private final AtomicInteger createEntityManagerCalls = new AtomicInteger();
        private final AtomicInteger beginCalls = new AtomicInteger();
        private final AtomicInteger flushCalls = new AtomicInteger();
        private final AtomicInteger commitCalls = new AtomicInteger();
        private final AtomicInteger rollbackCalls = new AtomicInteger();
        private final AtomicInteger entityManagerCloseCalls = new AtomicInteger();
        private final AtomicInteger entityManagerFactoryCloseCalls = new AtomicInteger();
        private final AtomicInteger providerCloseCalls = new AtomicInteger();
        private final AtomicBoolean transactionActive = new AtomicBoolean();
        private final AtomicBoolean entityManagerOpen = new AtomicBoolean(true);
        private final AtomicBoolean entityManagerFactoryOpen = new AtomicBoolean(true);

        private EntityManagerFactory entityManagerFactory() {
            EntityTransaction transaction = proxy(EntityTransaction.class, (methodName, returnType) -> switch (methodName) {
                case "begin" -> {
                    beginCalls.incrementAndGet();
                    transactionActive.set(true);
                    yield null;
                }
                case "commit" -> {
                    commitCalls.incrementAndGet();
                    transactionActive.set(false);
                    yield null;
                }
                case "rollback" -> {
                    rollbackCalls.incrementAndGet();
                    transactionActive.set(false);
                    yield null;
                }
                case "isActive" -> transactionActive.get();
                default -> defaultValue(returnType);
            });

            EntityManager entityManager = proxy(EntityManager.class, (methodName, returnType) -> switch (methodName) {
                case "getTransaction" -> transaction;
                case "flush" -> {
                    flushCalls.incrementAndGet();
                    yield null;
                }
                case "close" -> {
                    entityManagerCloseCalls.incrementAndGet();
                    entityManagerOpen.set(false);
                    yield null;
                }
                case "isOpen" -> entityManagerOpen.get();
                default -> defaultValue(returnType);
            });

            return proxy(EntityManagerFactory.class, (methodName, returnType) -> switch (methodName) {
                case "createEntityManager" -> {
                    createEntityManagerCalls.incrementAndGet();
                    entityManagerOpen.set(true);
                    yield entityManager;
                }
                case "close" -> {
                    entityManagerFactoryCloseCalls.incrementAndGet();
                    entityManagerFactoryOpen.set(false);
                    yield null;
                }
                case "isOpen" -> entityManagerFactoryOpen.get();
                default -> defaultValue(returnType);
            });
        }
    }

    @FunctionalInterface
    private interface Invocation {
        Object invoke(String methodName, Class<?> returnType);
    }

    @SuppressWarnings("unchecked")
    private static <T> T proxy(Class<T> type, Invocation invocation) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[]{type},
                (proxy, method, arguments) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return switch (method.getName()) {
                            case "toString" -> type.getSimpleName() + "Proxy";
                            case "hashCode" -> System.identityHashCode(proxy);
                            case "equals" -> proxy == arguments[0];
                            default -> null;
                        };
                    }
                    return invocation.invoke(method.getName(), method.getReturnType());
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (!returnType.isPrimitive()) {
            return null;
        }
        if (returnType == boolean.class) {
            return false;
        }
        if (returnType == byte.class) {
            return (byte) 0;
        }
        if (returnType == short.class) {
            return (short) 0;
        }
        if (returnType == int.class) {
            return 0;
        }
        if (returnType == long.class) {
            return 0L;
        }
        if (returnType == float.class) {
            return 0.0F;
        }
        if (returnType == double.class) {
            return 0.0D;
        }
        if (returnType == char.class) {
            return '\0';
        }
        return null;
    }
}
