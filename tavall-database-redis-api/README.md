# tavall-database-redis-api

Defines the client-free Redis contracts for Tavall Database: typed string operations, expiring leases, fenced versioned records, connection lifecycle, and the provider SPI. It contains no concrete Redis client.

## Responsibility

### Owns
- `IRedisDatabase`, `IRedisDatabaseBuilder`, and `IRedisConfigData`, the provider-neutral database surface.
- `IRedisConnectionHandler`: connection availability and close semantics.
- `IRedisQueryHandler`: typed `get`, `set`, `setIfAbsent`, `delete`, `expire`, and `timeToLive` over `RedisKey`.
- `IRedisLeaseHandler`, `RedisLease`, and `RedisLeaseState`: token-checked expiring leases.
- `IRedisVersionedRecordHandler`, `RedisVersionedRecord`, `RedisRecordFence`, `RedisRecordWriteResult`, and `RedisRecordWriteState`: fenced create, compare-and-set, and delete.
- `RedisKey`: bounded, non-blank, control-character-free key type.
- `IRedisDatabaseProvider` SPI and `RedisDatabaseProviderLoader`: exactly-one provider selection at runtime.
- `RedisConnectionException`, `RedisDatabaseException`, and `RedisQueryException`.

### Does Not Own
- A concrete Redis client, connection pool, or Jedis type. Those belong to `tavall-database-redis`.
- Domain key layout, payload schemas, indexes, projections, and reconciliation policy. Those stay with the domain owner that calls the API.
- Durable authority. Leases coordinate work; the domain owner's durable store and fence decide what is current.
- Composition of a runtime. The owning consumer runtime selects and registers the provider.

## Repository Structure

tavall-database/
├── [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md)
├── [`tavall-database-postgres`](../tavall-database-postgres/README.md)
├── [`tavall-database-mongo`](../tavall-database-mongo/README.md)
├── **[`tavall-database-redis-api`](README.md) ← This Module**
├── [`tavall-database-redis`](../tavall-database-redis/README.md)
├── [`tavall-database-qdrant`](../tavall-database-qdrant/README.md)
├── [`tavall-database-core`](../tavall-database-core/README.md)
└── [`tavall-database-test-suite`](../tavall-database-test-suite/README.md)

## Public Interfaces

| Type | Role |
| --- | --- |
| `IRedisDatabase` | Entry point. Exposes `connections()`, `queries()`, `leases()`, and `records()`. |
| `IRedisDatabaseBuilder` | Provider-neutral builder with `host`, `port`, `databaseIndex`, `tls`, and `build()`. |
| `IRedisConfigData` | Read-only connection configuration (host, port, credentials, database index, TLS). |
| `IRedisConnectionHandler` | `isAvailable()` and idempotent `close()`. |
| `IRedisQueryHandler` | Typed string operations with expiry. Inherits SQL-shaped `IDatabaseQueryHandler` methods only for `IDatabase` compatibility; Redis providers report them as unsupported. |
| `IRedisLeaseHandler` | `acquire`, `renew`, `release`, `holder`. |
| `IRedisVersionedRecordHandler` | `read`, `create`, `compareAndSet`, `delete`. |
| `IRedisDatabaseProvider` | Provider SPI: `providerId()` and `createBuilder()`. |
| `RedisDatabaseProviderLoader` | Resolves the single provider on the runtime classpath via `ServiceLoader`. |

## Dependency Rules

- `tavall-database-redis-api` depends on `tavall-database-core-contracts` only (`api`).
- No concrete Redis client may reach the API compile classpath. `verifyClientFreeApi` fails `check` when `redis.clients:*`, `io.lettuce:*`, or `org.redisson:*` appears.
- `tavall-database-redis` depends on this module and on Jedis. It implements the SPI and registers `JedisRedisDatabaseProvider` under `META-INF/services`.
- `tavall-database-core` aggregates this module and `tavall-database-redis`.
- Consumers compile against this module and receive a provider at runtime. Filesystem modules must not depend on it.

## Lifecycle and Semantics

### Connection
- `isAvailable()` never throws for connectivity loss; it returns `false`.
- `close()` is idempotent and releases provider-owned pools. Any later operation fails with `RedisConnectionException`.
- Callers never close a shared pool themselves.

### Leases
- `acquire` is an atomic set-if-absent with expiry. It returns empty when another acquisition holds the key.
- Every acquisition returns a `RedisLease` with a unique token. A holder whose lease expired and was re-acquired, including by the same owner, cannot renew or release it.
- `renew` and `release` return `RedisLeaseState.HELD` when applied and `LOST` when nothing changed.
- A lease is coordination, not durable authority. Durable writes still require the domain owner's fence.

### Fenced records
- A record carries `version` (starting at 1), `fenceEpoch` (writer authority, for example a lease generation), and an opaque `payload`.
- `compareAndSet(key, expected, next)` applies only when the stored version and fence epoch equal `expected`.
- `next` must advance the version and must not lower the fence epoch. Violations are rejected before Redis is contacted.
- Outcomes:
  - `APPLIED`: the write was applied; `current` holds the new record.
  - `EXISTS`: `create` found a record; the existing record is returned unchanged.
  - `STALE`: the stored version or fence epoch did not match the caller's fence.
  - `MISSING`: `compareAndSet` or `delete` targeted a record that does not exist.
- Each operation runs as one server-side script, so concurrent writers serialize and stale writers do not partially apply.

## Runtime Provider Selection

Provider selection happens at a composition boundary in the owning consumer runtime, not in domain code. The runtime resolves the single provider, builds the database, and registers the `IRedisDatabase` with Tavall DI. Ordinary consumers never call the loader.

```java
IRedisDatabase redis = new RedisDatabaseProviderLoader(runtimeClassLoader)
        .createBuilder()
        .host(configuredHost)
        .port(configuredPort)
        .build()
        .orElseThrow();
// Register `redis` with Tavall DI at the runtime composition root.
```

The loader fails fast when zero or several providers are present. Exactly one provider must be on the runtime classpath.

## Integration Example

A domain handler declares its Redis collaborator through `DependencyAccess<IRedisDatabase>`. It acquires a lease for mutual exclusion and then writes through a fenced compare-and-set. The domain owns the key names, payload, and version/epoch policy. Imports are omitted. With a single managed dependency, Tavall DI's `getInstance()` returns the typed contract directly; the consuming runtime registers the built `IRedisDatabase` in its generation-owned dependency map.

```java
@DelegatesTo(ICampaignStateHandler.class)
public final class CampaignStateHandler implements ICampaignStateHandler, DependencyAccess<IRedisDatabase> {

    private static final Duration LEASE_TIME_TO_LIVE = Duration.ofSeconds(30);

    @Override
    public void advance(String campaignId, String workerId, String nextPayload) {
        IRedisDatabase redis = getInstance();
        RedisKey leaseKey = RedisKey.of("campaign", campaignId, "lease");
        RedisKey stateKey = RedisKey.of("campaign", campaignId, "state");

        RedisLease lease = redis.leases()
                .acquire(leaseKey, workerId, LEASE_TIME_TO_LIVE)
                .orElseThrow(() -> new IllegalStateException("Campaign is busy: " + campaignId));
        try {
            RedisVersionedRecord current = redis.records()
                    .read(stateKey)
                    .orElseThrow(() -> new IllegalStateException("Campaign state missing: " + campaignId));

            // Checked inside Redis: this acquisition still holds the lease and the stored epoch is not newer
            // than lease.fencingToken(); the written record carries that token as its epoch.
            RedisRecordWriteResult result = redis.records()
                    .compareAndSetUnderLease(stateKey, current.fence(), current.version() + 1, nextPayload, lease);
            if (!result.applied()) {
                throw new IllegalStateException("Fenced write rejected: " + result.state());
            }
        } finally {
            redis.leases().release(lease);
        }
    }
}
```

## Compatibility and Migration

Version `1.0.0` to `1.1.0` is a source-breaking change for raw-client consumers.

| Area | 1.0.0 | 1.1.0 |
| --- | --- | --- |
| Connection handler | `IRedisConnectionHandler` exposed `openClient()` and `closeClient(JedisPooled)`. | `IRedisConnectionHandler` is client-free (`isAvailable()`, `close()`). The Jedis methods moved to `IJedisRedisConnectionHandler` in `tavall-database-redis`. |
| Raw client access | Available on the common interface. | `RedisDatabaseBuilder.buildJedis()` returns `IJedisRedisDatabase`, whose `connections()` returns `IJedisRedisConnectionHandler`. |
| Generic build | `build()` returned the Redis database. | `build()` still returns `Optional<IRedisDatabase>`; use `buildJedis()` only where raw Jedis is required. |
| Typed capabilities | Not in the common contract. | `leases()` and `records()` added, with typed `IRedisQueryHandler` operations. |
| Implementers of the interfaces | — | `IRedisDatabase` gained abstract `leases()` and `records()`; `IRedisQueryHandler` gained six abstract typed methods. Custom implementations and test fakes must implement them (source break). |
| Provider constructors | `RedisDatabase(IRedisConfigData, IRedisConnectionHandler, IRedisQueryHandler)`, `RedisQueryHandler(IRedisConnectionHandler)` | Parameters are now `IJedisRedisConnectionHandler`. Callers recompile against 1.1.0 (binary break). |
| Lease record | — | `RedisLease` gained a positional `long fencingToken` before `timeToLive` (source and binary break for constructor callers); it is drawn per key at acquisition. |
| Lease-fenced writes | — | `IRedisVersionedRecordHandler` gained `createUnderLease` and `compareAndSetUnderLease` (source break for implementers). |
| Exceptions | Jedis exceptions escaped the typed calls. | Provider calls map Jedis failures to `RedisConnectionException` / `RedisQueryException`; callers catching `JedisException` or `IllegalStateException` must catch the Redis API exceptions. |

Migration:
- Consumers that only need Redis operations: depend on `tavall-database-redis-api` and use `IRedisDatabase`.
- Consumers that still issue raw commands: use `RedisDatabaseBuilder.buildJedis()` and `IJedisRedisDatabase`, and depend on `tavall-database-redis` explicitly.

Removal condition for the `IJedis*` compatibility types: remove them once Tavall Cloud and Tavall MC raw-Jedis consumers migrate to the typed `IRedisDatabase` capabilities. Until then they are the documented compatibility boundary.

## Tests and Evidence

Evidence is taken from commit [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124) on `working/redis-api-module-20261009`. The results below were first recorded in that commit message. After the PR #30 reviews, `RedisDatabaseContractTest` has 10 tests and passes 10/10 against `redis:8-alpine`. The former "restart recovery" test is named reconnect recovery, because it reopens the provider against the same running Redis.

Contract tests are `RedisDatabaseContractTest` in `tavall-database-test-suite`, run against a real `redis:8-alpine` container through Testcontainers (10 tests):

- `providerLoaderSelectsTheSingleRuntimeProvider`
- `publicApiExposesNoConcreteClientTypes`
- `typedValuesRoundTripAndExpire`
- `setIfAbsentAdmitsExactlyOneConcurrentWriter`
- `leaseTokensIsolateStaleHoldersAfterExpiry`
- `fencedRecordsRejectStaleAndMissingWrites`
- `concurrentCompareAndSetAppliesOneWriterPerVersion`
- `stateSurvivesProviderReconnectAndClosedHandlersFailFast`
- `leaseFencingTokensIncreaseAndStaleHoldersCannotOverwriteNewerRecords`
- `leaseFencedWritesRejectAnExpiredHolderBeforeAndAfterTheNextHolderWrites`

Other recorded evidence: `./gradlew --write-locks clean check` succeeded; `CanonicalArchitectureTest` 1/1 and `DatabaseBuilderTypingTest` 5/5 passed; remote smoke tests were skipped because remote database environment variables are not configured.

Build enforcement: `verifyClientFreeApi` runs as part of `check`.

## Risks

- Source-breaking for 1.0.0 raw-Jedis consumers until they migrate; the `IJedis*` compatibility types keep them compiling only when they depend on `tavall-database-redis`.
- Leases are advisory coordination. A process that pauses past its lease TTL can still attempt a write. Use `createUnderLease` / `compareAndSetUnderLease`: they reject the write inside Redis unless this exact acquisition still holds the lease and the stored epoch is not newer than its fencing token. Plain `create` / `compareAndSet` do not consult a lease.
- The fencing counter (`<lease key>:fencing-token`) must be as durable as the records it fences: run Redis with `noeviction` and persistence. If the counter is evicted or deleted, the next token restarts at 1 and lease-fenced writes return `STALE` against newer stored epochs until the counter is restored above them.
- The provider targets standalone Redis. Under Redis Cluster the lease key, its counter, and a fenced record would need one hash slot.
- Generic `queries()` operations can address any key, including lease and record keys, and would bypass their token and fence checks. Keep lease and record key families separate from generic string keys.
- `RedisVersionedRecord.payload` is opaque to this module. Schema drift is the domain owner's risk.
- The module has no module-local `.tavallci/ci.yaml` in this repository yet, so CI ownership is not declared at module level.
- Remote Redis compatibility (managed services, TLS endpoints, cluster topology) was not exercised.

## Relationships

| Module / System | Relationship |
| --- | --- |
| [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md) | Upstream contract dependency (`api`). |
| [`tavall-database-redis`](../tavall-database-redis/README.md) | Jedis provider; implements this API and registers the SPI. |
| [`tavall-database-core`](../tavall-database-core/README.md) | Aggregates this API with the Redis provider. |
| [`tavall-database-test-suite`](../tavall-database-test-suite/README.md) | Hosts the Redis contract tests and applies architecture tests. |
| Owning consumer runtime (Tavall Cloud, Tavall MC) | Composes the provider and consumes `IRedisDatabase`. Not named in this repository. |

## Documentation

| Type | Document | Purpose | Surface |
| --- | --- | --- | --- |
| GENERAL | [Repository README](../README.md) | Public overview and module map. | GitHub |
| Technical | [Contribution guide](../CONTRIBUTING.md) | Repository-specific development and validation. | GitHub |
| Progression | [Tavall Database Redis API Progression](../docs/progression/TAVALL_DATABASE_REDIS_API_PROGRESSION.md) | Module implementation, validation, and history. | GitHub |
| Progression | [Tavall Database Redis Progression](../docs/progression/TAVALL_DATABASE_REDIS_PROGRESSION.md) | Provider implementation and compatibility state. | GitHub |
| Progression | [Tavall Database System Progression](../docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md) | Cross-module architecture and system acceptance. | GitHub |

## Deployment

> This module is not independently deployed.

Runtime owner: the owning consumer runtime composes this API. No Deployment record applies to this library boundary.

## Development

- **Module Type:** `API`
- **Runtime:** Owning consumer runtime (not deployed by this module)
- **CI Definition:** Module-local `.tavallci/ci.yaml` is not present yet; required by the README standards and tracked as a progression blocker.
- **Current PR Stack:** [PR #30](https://github.com/TavallStudios/tavall-database/pull/30) (`working/redis-api-module-20261009`). Not published to GitHub Packages or the internal repository.
- **Progression:** [Module Progression](../docs/progression/TAVALL_DATABASE_REDIS_API_PROGRESSION.md) · [System Progression](../docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md).
- Repository-specific development guide: [CONTRIBUTING.md](../CONTRIBUTING.md).

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `TEMPORARY_DRIFT` | `TavallStudios/tavall-database/tavall-database-redis-api/README.md` | 2026-10-09 3:30 PM PDT | Open [PR #30](https://github.com/TavallStudios/tavall-database/pull/30); review fixes for atomic CAS results and lease fencing tokens. |
| Notion | `NOT_APPLICABLE` | — | 2026-10-09 2:13 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-10-09 2:13 PM PDT | GitHub | `CREATED` | `tavall-database-redis-api/README.md` | — | Commit `98b9312` and documentation commit on `working/redis-api-module-20261009` | Added the module README for the client-free Redis API, migration notes, and evidence routing. |

</details>
