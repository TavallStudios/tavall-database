# tavall-database-redis-api Progression

> **Status:** Active progression record  
> **Document Type:** `PROGRESSION`  
> **Progression Scope:** `MODULE`  
> **Module Type:** `API`  
> **Owning System:** `Tavall Database`  
> **Owns:** Audited implementation, contract validation, compatibility, and historical progression for `tavall-database-redis-api`  
> **Does Not Own:** Aggregate system progression, deployment history, product/design rules, or Git workflow policy  
> **Audited Against:** `working/redis-api-module-20261009@98b9312c942c192e841e0c26183b64107fb06124` (open [PR #30](https://github.com/TavallStudios/tavall-database/pull/30))  
> **Last Reconciled:** `2026-10-09 2:13 PM PDT`

## About

`tavall-database-redis-api` is an `API` module responsible for the client-free Redis contracts within `Tavall Database`: typed string operations, expiring leases, fenced versioned records, connection lifecycle, and the runtime provider SPI.

For an API module, progression measures contract implementation, exposed operations, consumer adoption, compatibility, and contract validation. Consumer adoption is not yet established.

## Module Context

| Field | Value |
| --- | --- |
| Repository | [TavallStudios/tavall-database](https://github.com/TavallStudios/tavall-database) |
| Module | `tavall-database-redis-api` |
| Module Type | `API` |
| Owning System | `Tavall Database` |
| System Progression | [Tavall Database System Progression](TAVALL_DATABASE_SYSTEM_PROGRESSION.md) |
| Runtime Owner | Owning consumer runtime composes the provider; this module is not deployed. |
| Primary Consumers | Tavall Cloud and Tavall MC are the expected consumers. Neither has migrated yet. |
| Provider | [`tavall-database-redis`](TAVALL_DATABASE_REDIS_PROGRESSION.md) (Jedis, secondary role `PROVIDER`) |
| Current Branch / PR Stack | [PR #30](https://github.com/TavallStudios/tavall-database/pull/30) (`working/redis-api-module-20261009`); not published to GitHub Packages or the internal repository. |
| Audited Revision | [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124) |

## Current Status

| Field | State |
| --- | --- |
| Overall State | `VALIDATED` (local only) |
| Current Phase | Contracts implemented and validated locally; consumer migration not started. |
| Implementation | Public contracts, lease and fenced-record types, `RedisKey`, provider SPI, and `RedisDatabaseProviderLoader` are present in source. |
| Integration | Consumed by `tavall-database-core` and `tavall-database-test-suite`; the Jedis provider implements it. |
| Validation | Local `./gradlew --write-locks clean check` passed per commit `98b9312`; `RedisDatabaseContractTest` 8/8 against `redis:8-alpine`. |
| Runtime / Consumer Acceptance | Not established. No consuming runtime has adopted the typed capabilities. |
| Deployment Verification | `N/A` for this non-deployed API module. |
| Primary Blocker | No module-local `.tavallci/ci.yaml`; no published artifact; consumers not yet migrated from 1.0.0 raw-Jedis usage. |
| Next Slice | Add the module CI definition, push through the review path, then migrate Tavall Cloud and Tavall MC to typed `IRedisDatabase` capabilities. |

## Progression Timeline

| Date / Time | State | Progression | Evidence | Result / Remaining Work |
| --- | --- | --- | --- | --- |
| `2026-10-09 2:13 PM PDT` | `VALIDATED` | Introduced `tavall-database-redis-api` with client-free `IRedisDatabase`, builder and configuration contracts, `IRedisConnectionHandler`, typed `IRedisQueryHandler`, `IRedisLeaseHandler`, `IRedisVersionedRecordHandler`, `RedisKey`, the `IRedisDatabaseProvider` SPI, and `RedisDatabaseProviderLoader`; moved Redis contracts out of the Jedis module with their FQNs; applied `verifyClientFreeApi`. | [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124) on `working/redis-api-module-20261009` | Validated locally per the commit: `./gradlew --write-locks clean check` succeeded and `RedisDatabaseContractTest` passed 8/8. Local only: not pushed, not published to GitHub Packages, consumers not yet migrated. |
| `2026-10-09 3:30 PM PDT` | `VALIDATED` | Review fixes: write results return the current record from inside the same Lua script; `RedisLease.fencingToken` is drawn from a per-key `INCR` at acquisition so stale holders are fenced; Jedis failures map to `RedisConnectionException`/`RedisQueryException`; plugin repository follows `TAVALL_INTERNAL_PRIVATE_REPOSITORY_URL`. | [PR #30](https://github.com/TavallStudios/tavall-database/pull/30) | `./gradlew --no-build-cache clean check` passed; `RedisDatabaseContractTest` 9/9. Tavall Cloud composite consumes this source (TavallStudios/tavall-cloud#434). |

## Validation State

| Validation | State | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Architecture | Passed locally | `verifyClientFreeApi` in `check`; `CanonicalArchitectureTest` 1/1 (per commit `98b9312`) | Run in module CI once defined |
| Client-free API | Passed locally | `publicApiExposesNoConcreteClientTypes` in `RedisDatabaseContractTest` | Keep enforced on future API changes |
| Contract (Testcontainers) | Passed locally | `RedisDatabaseContractTest` 11/11 against `redis:8-alpine`: lease-fenced writes (expired holder with and without a next holder, epoch guard), provider selection, expiry, concurrent `setIfAbsent`, lease isolation after expiry, fenced CAS stale/missing/epoch, concurrent CAS, lease fencing tokens with a stale holder rejected, reconnect recovery, closed-handler failure | Re-run in CI; remote services not exercised |
| Unit | Not separately recorded | No module-local test sources | Add only if API-level behavior needs direct coverage |
| Consumer / Runtime | Not verified | No consumer has migrated | Migrate Tavall Cloud and Tavall MC; record acceptance |
| End-to-End | Not established | No deployed runtime | Record in owning runtime progression when adopted |

## Dependencies and Integration

| Dependency / Consumer | Relationship | State | Evidence |
| --- | --- | --- | --- |
| `tavall-database-core-contracts` | Upstream `api` dependency | Declared in root build | [`build.gradle.kts`](https://github.com/TavallStudios/tavall-database/blob/98b9312c942c192e841e0c26183b64107fb06124/build.gradle.kts) |
| `tavall-database-redis` | Jedis provider; implements this API and the SPI | Local; registered via `META-INF/services` | [`TAVALL_DATABASE_REDIS_PROGRESSION.md`](TAVALL_DATABASE_REDIS_PROGRESSION.md) |
| `tavall-database-core` | Aggregates this API with the Redis provider | Declared in root build | Root build |
| `tavall-database-test-suite` | Hosts `RedisDatabaseContractTest` and applies architecture tests | Passed locally | Commit `98b9312` |
| Tavall Cloud, Tavall MC | Expected typed consumers; currently raw-Jedis users of 1.0.0 | Not migrated | Migration tracked here and in the Redis Progression |

## Blockers

| Blocker | Impact | Resolution |
| --- | --- | --- |
| No module-local `.tavallci/ci.yaml` | Module CI ownership is not declared, so the module has no CI-gated evidence. | Add the module CI definition in a CI-scoped change. |
| Not published | Consumers resolve 1.1.0 only through exact-source composites. | Publish after PR #30 merges. |
| Consumers still on 1.0.0 raw-Jedis APIs | The `IJedis*` compatibility types cannot be removed. | Migrate Tavall Cloud and Tavall MC to typed capabilities, then remove the compatibility types. |

## Next Slice

Add the module `.tavallci/ci.yaml`, obtain CI-gated evidence for the exact pushed head, then migrate the raw-Jedis consumers to `IRedisDatabase` leases and fenced records. Remove `IJedisRedisDatabase` and `IJedisRedisConnectionHandler` only after that migration.

## Related Documentation

| Type | Document |
| --- | --- |
| Module README | [`README.md`](../../tavall-database-redis-api/README.md) |
| Provider Progression | [`TAVALL_DATABASE_REDIS_PROGRESSION.md`](TAVALL_DATABASE_REDIS_PROGRESSION.md) |
| Owning System Progression | [`TAVALL_DATABASE_SYSTEM_PROGRESSION.md`](TAVALL_DATABASE_SYSTEM_PROGRESSION.md) |
| Design / Final | N/A (not authored in this repository) |
| Technical | N/A (not authored in this repository) |
| Deployment | N/A (not independently deployed) |

## Documentation Update State

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `TEMPORARY_DRIFT` | `docs/progression/TAVALL_DATABASE_REDIS_API_PROGRESSION.md` in `working/redis-api-module-20261009` | 2026-10-10 | Open [PR #30](https://github.com/TavallStudios/tavall-database/pull/30); lease-fenced writes added after re-review. |
| Notion | `TEMPORARY_DRIFT` | Required twin not created | 2026-10-09 2:13 PM PDT | Pending GitHub publication; no Notion page was created in this change. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-10-09 2:13 PM PDT | GitHub | `CREATED` | `docs/progression/TAVALL_DATABASE_REDIS_API_PROGRESSION.md` | — | Commit `98b9312` on `working/redis-api-module-20261009` | Created module Progression for the client-free Redis API. |

</details>
