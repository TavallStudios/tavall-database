package org.tavall.database.redis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.tavall.database.redis.connection.IRedisConnectionHandler;
import org.tavall.database.redis.exception.RedisConnectionException;
import org.tavall.database.redis.key.RedisKey;
import org.tavall.database.redis.lease.IRedisLeaseHandler;
import org.tavall.database.redis.lease.RedisLease;
import org.tavall.database.redis.lease.RedisLeaseState;
import org.tavall.database.redis.provider.IRedisDatabaseProvider;
import org.tavall.database.redis.provider.JedisRedisDatabaseProvider;
import org.tavall.database.redis.provider.RedisDatabaseProviderLoader;
import org.tavall.database.redis.query.IRedisQueryHandler;
import org.tavall.database.redis.record.IRedisVersionedRecordHandler;
import org.tavall.database.redis.record.RedisRecordFence;
import org.tavall.database.redis.record.RedisRecordWriteResult;
import org.tavall.database.redis.record.RedisRecordWriteState;
import org.tavall.database.redis.record.RedisVersionedRecord;

/** Redis API contract verified against a real Redis server through the runtime-selected provider. */
@Testcontainers
class RedisDatabaseContractTest {

    @Container
    private static final GenericContainer<?> REDIS = new GenericContainer<>("redis:8-alpine").withExposedPorts(6379);

    private IRedisDatabase database;

    @BeforeEach
    void connect() {
        database = openDatabase();
        assertTrue(database.isAvailable());
    }

    @AfterEach
    void close() {
        database.close();
    }

    @Test
    void providerLoaderSelectsTheSingleRuntimeProvider() {
        IRedisDatabaseProvider provider = new RedisDatabaseProviderLoader(getClass().getClassLoader()).resolve();

        assertEquals(JedisRedisDatabaseProvider.PROVIDER_ID, provider.providerId());
    }

    @Test
    void publicApiExposesNoConcreteClientTypes() {
        for (Class<?> contract : List.of(IRedisDatabase.class, IRedisDatabaseBuilder.class, IRedisConfigData.class,
                IRedisConnectionHandler.class, IRedisQueryHandler.class, IRedisLeaseHandler.class,
                IRedisVersionedRecordHandler.class, IRedisDatabaseProvider.class)) {
            for (Method method : contract.getMethods()) {
                List<Class<?>> types = new ArrayList<>(List.of(method.getReturnType()));
                Stream.of(method.getParameters()).map(Parameter::getType).forEach(types::add);
                for (Class<?> type : types) {
                    assertFalse(type.getName().startsWith("redis.clients."),
                            contract.getSimpleName() + "." + method.getName() + " exposes " + type.getName());
                }
            }
        }
    }

    @Test
    void typedValuesRoundTripAndExpire() throws InterruptedException {
        IRedisQueryHandler queries = database.queries();
        RedisKey key = RedisKey.of("tavall-database-test", "value", "expiring");

        queries.set(key, "first", Duration.ofMillis(300));
        assertEquals(Optional.of("first"), queries.get(key));
        assertTrue(queries.timeToLive(key).isPresent());

        Thread.sleep(500);

        assertEquals(Optional.empty(), queries.get(key));
        assertFalse(queries.delete(key));
        assertFalse(queries.expire(key, Duration.ofSeconds(1)));
    }

    @Test
    void setIfAbsentAdmitsExactlyOneConcurrentWriter() throws Exception {
        RedisKey key = RedisKey.of("tavall-database-test", "claim", "concurrent");
        List<Boolean> outcomes = runConcurrently(16, index ->
                database.queries().setIfAbsent(key, "writer-" + index, Duration.ofSeconds(30)));

        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
    }

    @Test
    void leaseTokensIsolateStaleHoldersAfterExpiry() throws InterruptedException {
        IRedisLeaseHandler leases = database.leases();
        RedisKey key = RedisKey.of("tavall-database-test", "lease", "authority");

        RedisLease first = leases.acquire(key, "node-a", Duration.ofMillis(200)).orElseThrow();
        assertTrue(leases.acquire(key, "node-b", Duration.ofSeconds(5)).isEmpty());
        assertEquals(RedisLeaseState.HELD, leases.renew(first, Duration.ofMillis(200)));
        assertEquals(Optional.of("node-a"), leases.holder(key));

        Thread.sleep(400);

        RedisLease second = leases.acquire(key, "node-a", Duration.ofSeconds(5)).orElseThrow();
        assertEquals(RedisLeaseState.LOST, leases.renew(first, Duration.ofSeconds(5)));
        assertEquals(RedisLeaseState.LOST, leases.release(first));
        assertEquals(Optional.of("node-a"), leases.holder(key));
        assertEquals(RedisLeaseState.HELD, leases.release(second));
        assertEquals(Optional.empty(), leases.holder(key));
    }

    @Test
    void leaseFencingTokensIncreaseAndStaleHoldersCannotOverwriteNewerRecords() throws InterruptedException {
        IRedisLeaseHandler leases = database.leases();
        IRedisVersionedRecordHandler records = database.records();
        RedisKey leaseKey = RedisKey.of("tavall-database-test", "lease", "fenced-writer");
        RedisKey recordKey = RedisKey.of("tavall-database-test", "record", "fenced-writer");

        RedisLease paused = leases.acquire(leaseKey, "writer-a", Duration.ofMillis(150)).orElseThrow();
        records.create(recordKey, new RedisVersionedRecord(1, paused.fencingToken(), "a-1"));
        Thread.sleep(300);
        RedisLease current = leases.acquire(leaseKey, "writer-b", Duration.ofSeconds(5)).orElseThrow();
        assertTrue(current.fencingToken() > paused.fencingToken());

        RedisVersionedRecord seenByB = records.read(recordKey).orElseThrow();
        RedisRecordWriteResult written = records.compareAndSet(recordKey, seenByB.fence(),
                new RedisVersionedRecord(2, current.fencingToken(), "b-2"));
        assertTrue(written.applied());
        assertEquals(Optional.of(new RedisVersionedRecord(2, current.fencingToken(), "b-2")), written.current());

        // Writer A resumes after its lease expired: its own lower fencing token can never replace B's record.
        RedisVersionedRecord seenByA = records.read(recordKey).orElseThrow();
        assertThrows(IllegalArgumentException.class, () -> records.compareAndSet(recordKey, seenByA.fence(),
                new RedisVersionedRecord(3, paused.fencingToken(), "a-3")));
        assertEquals("b-2", records.read(recordKey).orElseThrow().payload());
    }

    @Test
    void leaseFencedWritesRejectAnExpiredHolderBeforeAndAfterTheNextHolderWrites() throws InterruptedException {
        IRedisLeaseHandler leases = database.leases();
        IRedisVersionedRecordHandler records = database.records();
        RedisKey leaseKey = RedisKey.of("tavall-database-test", "lease", "server-fenced");
        RedisKey recordKey = RedisKey.of("tavall-database-test", "record", "server-fenced");

        RedisLease paused = leases.acquire(leaseKey, "writer-a", Duration.ofMillis(500)).orElseThrow();
        assertTrue(records.createUnderLease(recordKey, 1, "a-1", paused).applied());
        RedisVersionedRecord afterCreate = records.read(recordKey).orElseThrow();
        assertEquals(paused.fencingToken(), afterCreate.fenceEpoch());
        Thread.sleep(800);
        // Expired with no next holder yet.
        assertEquals(RedisRecordWriteState.STALE,
                records.compareAndSetUnderLease(recordKey, afterCreate.fence(), 2, "a-expired", paused).state());
        RedisLease current = leases.acquire(leaseKey, "writer-b", Duration.ofSeconds(5)).orElseThrow();

        // S1: the expired holder writes before the new holder has written anything.
        RedisRecordWriteResult early = records.compareAndSetUnderLease(recordKey, afterCreate.fence(), 2, "a-2", paused);
        assertEquals(RedisRecordWriteState.STALE, early.state());
        assertEquals(Optional.of(afterCreate), early.current());

        RedisRecordWriteResult written = records.compareAndSetUnderLease(recordKey, afterCreate.fence(), 2, "b-2", current);
        assertTrue(written.applied());
        assertEquals(current.fencingToken(), written.current().orElseThrow().fenceEpoch());

        // S2: the expired holder re-reads the newer record and retries with its fence.
        RedisVersionedRecord seen = records.read(recordKey).orElseThrow();
        assertEquals(RedisRecordWriteState.STALE,
                records.compareAndSetUnderLease(recordKey, seen.fence(), 3, "a-3", paused).state());
        RedisKey otherKey = RedisKey.of("tavall-database-test", "record", "other");
        assertEquals(RedisRecordWriteState.STALE, records.createUnderLease(otherKey, 1, "a", paused).state());
        assertTrue(records.read(otherKey).isEmpty());
        assertEquals("b-2", records.read(recordKey).orElseThrow().payload());
    }

    @Test
    void leaseFencedWritesNeverLowerTheStoredEpoch() {
        RedisKey leaseKey = RedisKey.of("tavall-database-test", "lease", "epoch-guard");
        RedisKey recordKey = RedisKey.of("tavall-database-test", "record", "epoch-guard");
        RedisVersionedRecord newer = new RedisVersionedRecord(1, 1_000, "written-under-a-later-epoch");
        database.records().create(recordKey, newer);
        RedisLease lease = database.leases().acquire(leaseKey, "writer", Duration.ofSeconds(5)).orElseThrow();

        RedisRecordWriteResult result = database.records()
                .compareAndSetUnderLease(recordKey, newer.fence(), 2, "lower-token", lease);

        assertEquals(RedisRecordWriteState.STALE, result.state());
        assertEquals(Optional.of(newer), result.current());
    }

    @Test
    void fencedRecordsRejectStaleAndMissingWrites() {
        IRedisVersionedRecordHandler records = database.records();
        RedisKey key = RedisKey.of("tavall-database-test", "record", "fenced");
        RedisVersionedRecord initial = new RedisVersionedRecord(1, 1, "{\"state\":\"initial\"}");

        assertEquals(RedisRecordWriteState.APPLIED, records.create(key, initial).state());
        RedisRecordWriteResult duplicate = records.create(key, new RedisVersionedRecord(1, 1, "other"));
        assertEquals(RedisRecordWriteState.EXISTS, duplicate.state());
        assertEquals(Optional.of(initial), duplicate.current());

        RedisVersionedRecord next = new RedisVersionedRecord(2, 2, "{\"state\":\"next\"}");
        assertTrue(records.compareAndSet(key, initial.fence(), next).applied());

        RedisRecordWriteResult stale = records.compareAndSet(key, initial.fence(), new RedisVersionedRecord(3, 2, "late"));
        assertEquals(RedisRecordWriteState.STALE, stale.state());
        assertEquals(Optional.of(next), stale.current());

        RedisRecordWriteResult staleEpoch = records.compareAndSet(key, new RedisRecordFence(2, 1),
                new RedisVersionedRecord(3, 2, "wrong-epoch"));
        assertEquals(RedisRecordWriteState.STALE, staleEpoch.state());

        assertThrows(IllegalArgumentException.class,
                () -> records.compareAndSet(key, next.fence(), new RedisVersionedRecord(2, 2, "no-advance")));
        assertThrows(IllegalArgumentException.class,
                () -> records.compareAndSet(key, next.fence(), new RedisVersionedRecord(3, 1, "lower-epoch")));

        assertEquals(RedisRecordWriteState.STALE, records.delete(key, initial.fence()).state());
        assertEquals(RedisRecordWriteState.APPLIED, records.delete(key, next.fence()).state());
        assertEquals(RedisRecordWriteState.MISSING, records.compareAndSet(key, next.fence(),
                new RedisVersionedRecord(3, 2, "gone")).state());
    }

    @Test
    void concurrentCompareAndSetAppliesOneWriterPerVersion() throws Exception {
        IRedisVersionedRecordHandler records = database.records();
        RedisKey key = RedisKey.of("tavall-database-test", "record", "contended");
        RedisVersionedRecord initial = new RedisVersionedRecord(1, 1, "seed");
        records.create(key, initial);

        List<Boolean> outcomes = runConcurrently(16, index -> records
                .compareAndSet(key, initial.fence(), new RedisVersionedRecord(2, 1, "writer-" + index))
                .applied());

        assertEquals(1, outcomes.stream().filter(Boolean::booleanValue).count());
        assertEquals(2, records.read(key).orElseThrow().version());
    }

    @Test
    void stateSurvivesProviderReconnectAndClosedHandlersFailFast() {
        RedisKey key = RedisKey.of("tavall-database-test", "record", "recovery");
        RedisVersionedRecord stored = new RedisVersionedRecord(7, 3, "{\"recovered\":true}");
        database.records().create(key, stored);
        database.close();

        assertFalse(database.isAvailable());
        assertThrows(RedisConnectionException.class, () -> database.records().read(key));

        database = openDatabase();
        assertEquals(Optional.of(stored), database.records().read(key));
    }

    private static IRedisDatabase openDatabase() {
        return new RedisDatabaseProviderLoader(RedisDatabaseContractTest.class.getClassLoader())
                .createBuilder()
                .host(REDIS.getHost())
                .port(REDIS.getMappedPort(6379))
                .build()
                .orElseThrow();
    }

    private static <T> List<T> runConcurrently(int writers, IndexedTask<T> task) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(writers);
        CountDownLatch start = new CountDownLatch(1);
        try {
            List<Future<T>> futures = new ArrayList<>();
            for (int index = 0; index < writers; index++) {
                int writer = index;
                Callable<T> call = () -> {
                    start.await();
                    return task.run(writer);
                };
                futures.add(executor.submit(call));
            }
            start.countDown();
            List<T> results = new ArrayList<>();
            for (Future<T> future : futures) {
                results.add(future.get(30, TimeUnit.SECONDS));
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    @FunctionalInterface
    private interface IndexedTask<T> {
        T run(int index) throws Exception;
    }
}
