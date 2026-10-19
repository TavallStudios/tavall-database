# Tavall Database System Progression

> **Status:** Active system progression record  
> **Document Type:** `PROGRESSION`  
> **Progression Scope:** `SYSTEM`  
> **System:** `Tavall Database`  
> **Owns:** Cross-module contract/provider architecture, consumer assembly, verification state, and system history  
> **Does Not Own:** Individual module implementation detail, remote database operations, or facts not evidenced in GitHub  
> **Audited Against:** current main merge commit `e0936fdbeaf9034830bd72d4e324e9a2c45218bd`; PR #29 source tree `4588b90b95160f6066247782466a6441ec7db649` is on main.
> **Last Reconciled:** `2026-10-19 3:36 PM PDT`

## About

Tavall Database is an eight-project Gradle system with shared database contracts, a client-free Redis API, four provider implementations, an aggregate consumer module, and a cross-provider test suite. No independently hosted database runtime is defined by these modules; consuming applications choose and configure providers.

This record tracks system boundaries and aggregate verification. See each module Progression for its source history and module test evidence.

## System Context

| Field | Value |
| --- | --- |
| Repository | [TavallStudios/tavall-database](https://github.com/TavallStudios/tavall-database) |
| Audited main revision | [`e0936fdbeaf9034830bd72d4e324e9a2c45218bd`](https://github.com/TavallStudios/tavall-database/commit/e0936fdbeaf9034830bd72d4e324e9a2c45218bd) |
| Build | Gradle multi-project; JDK 25 |
| Runtime owner | None; consuming applications own provider configuration and service endpoints. |
| Current PR context | Advisory-lock producer PR [#29](https://github.com/TavallStudios/tavall-database/pull/29) merged into main at `e0936fdbeaf9034830bd72d4e324e9a2c45218bd`. CI transition [#17](https://github.com/TavallStudios/tavall-database/pull/17) remains a separate staging path; PRs #27 and #30 are merged. |
| Overall state | `PARTIAL` |

## Current Status

| Area | State |
| --- | --- |
| Module boundaries | Eight Gradle subprojects are on main, including the client-free Redis API and its Jedis provider. |
| Implementation | tavall-database-postgres owns PostgreSQL/JPA transaction mechanics; no independent runtime is defined. |
| Verification | PR #29 merged to main with the tested source tree unchanged: merge `e0936fd`, PR source `fe9519a`, both tree `4588b90`. `:tavall-database-postgres:check --rerun-tasks` passed locally: 17 tests total (12 passed, 5 skipped), 0 failures/errors. |
| CI ownership | No .tavallci/ci.yaml is on main. Local CI transition PR #17 targets staging/platform; GitHub Actions is not Tavall CI compute. |
| Runtime integration | Tavall-MC PR #333 is the first source-composed consumer. PostgreSQL contention/rollback and package-backed consumer acceptance remain unverified. |
| Remaining acceptance | Run PostgreSQL integration against an explicitly disposable database and validate an immutable package-backed consumer after Tavall CI publishes the provider artifact. |

## Module Map

| Module | Type | Runtime owner | Boundary | Module Progression |
| --- | --- | --- | --- | --- |
| [`tavall-database-core-contracts`](../../tavall-database-core-contracts/README.md) | `API` | None | Shared database, builder, configuration, query and result contracts | [Progression](TAVALL_DATABASE_CORE_CONTRACTS_PROGRESSION.md) |
| [`tavall-database-core`](../../tavall-database-core/README.md) | `LIBRARY` | None | Aggregate dependency surface for consumers | [Progression](TAVALL_DATABASE_CORE_PROGRESSION.md) |
| [`tavall-database-postgres`](../../tavall-database-postgres/README.md) | `PROVIDER` | None | PostgreSQL and JPA provider | [Progression](TAVALL_DATABASE_POSTGRES_PROGRESSION.md) |
| [`tavall-database-mongo`](../../tavall-database-mongo/README.md) | `PROVIDER` | None | MongoDB provider | [Progression](TAVALL_DATABASE_MONGO_PROGRESSION.md) |
| [`tavall-database-redis-api`](../../tavall-database-redis-api/README.md) | `API` | Owning consumer runtime | Client-free Redis contracts, leases, fenced records, provider SPI | [Progression](TAVALL_DATABASE_REDIS_API_PROGRESSION.md) |
| [`tavall-database-redis`](../../tavall-database-redis/README.md) | `FEATURE` (secondary `PROVIDER`) | None | Jedis-backed Redis provider | [Progression](TAVALL_DATABASE_REDIS_PROGRESSION.md) |
| [`tavall-database-qdrant`](../../tavall-database-qdrant/README.md) | `PROVIDER` | None | Qdrant provider | [Progression](TAVALL_DATABASE_QDRANT_PROGRESSION.md) |
| [`tavall-database-test-suite`](../../tavall-database-test-suite/README.md) | `TEST_SUITE` | None | Cross-provider tests and remote-service smoke configurations | [Progression](TAVALL_DATABASE_TEST_SUITE_PROGRESSION.md) |

## Dependencies and Integration

| Boundary | Relationship | Evidence / state |
| --- | --- | --- |
| Contracts | Depends on Tavall Logging. | Declared in the root build; package resolution remains an external dependency boundary. |
| Core aggregate | Depends on contracts, the Redis API, and all four provider modules. | The current main source includes the Redis API/provider split. |
| Redis API | Client-free leases, fenced records and provider SPI; Redis provider is separate. | PR #30 merged at 7c768ca595447aa091da0724ef414b24bacc41bc; package publication and product consumer acceptance remain separate. |
| PostgreSQL advisory operation | Typed transaction-scoped lock on IPostgresEntityOperationContext; PostgreSQL SQL remains provider-owned. | PR #29 merged to current main at `e0936fd`; local provider check passed on the identical source tree, with five PostgreSQL-service tests skipped. |
| MongoDB, Redis and Qdrant providers | Vendor client libraries remain inside their provider modules; the aggregate assembles the providers. | Remote service behavior is not inferred from source or provider unit tests. |
| Test suite | Aggregates core/providers and tracks remote database test configurations. | The PostgreSQL provider check ran; other provider/system profiles were not run in this pass. |

## Progression Timeline

| Date / Time | State | Progression | Evidence | Result / Remaining Work |
| --- | --- | --- | --- | --- |
| 2026-05-30 11:07 PM PDT | `HISTORICAL_EVIDENCE` | Added the database Gradle reactor and the first provider/contracts source boundaries. | [`846dc7326483`](https://github.com/TavallStudios/tavall-database/commit/846dc732648381333e4229ea2e3afdd690a83fdc) | Establishes module/source history; no build or test result is implied. |
| 2026-05-30 11:18 PM PDT | `IN_PROGRESS` | Added shared lifecycle, builder, query and result-mapping contracts. | [`a61c1b6a8393`](https://github.com/TavallStudios/tavall-database/commit/a61c1b6a8393ba5df8d9da8d976d5ecee7195e4b) | Contracts form the provider API boundary; compatibility was not tested in this audit. |
| 2026-05-30 11:21 PM PDT | `IN_PROGRESS` | Added the aggregate supported database module. | [`cc13524e0f00`](https://github.com/TavallStudios/tavall-database/commit/cc13524e0f00cb3308f35fc0b529bc52289fbc32) | Consumers have an aggregate dependency surface; no consumer build was run. |
| 2026-05-30 11:48 PM PDT | `IN_PROGRESS` | Added MongoDB, Redis and Qdrant provider implementations. | [Qdrant commit](https://github.com/TavallStudios/tavall-database/commit/a9a9929d3b8f21f408eba70f25f5cd2ecd35ac5c) | The four-provider boundary is represented in source history; remote service compatibility is not established. |
| 2026-05-31 8:38 PM PDT | `IN_PROGRESS` | Added remote database fixtures and builder/backend test coverage. | [`adf09ba5a2eb`](https://github.com/TavallStudios/tavall-database/commit/adf09ba5a2eb4747b9cfedb52e01d06d7388e345) | Test sources are tracked; database test execution is not implied. |
| 2026-07-16 1:23 PM PDT | `IN_PROGRESS` | Merged TavallMonoRepo module history and live state. | [`500856b8ecaf`](https://github.com/TavallStudios/tavall-database/commit/500856b8ecaf8010c354fd5eea3ccfefe5778ff2) | Current history includes the multi-module source tree; this does not establish a release or passing build. |
| 2026-08-10 4:36 PM PDT | `IN_PROGRESS` | Added TLS support to Redis connections. | [`cc0cd3dbad70`](https://github.com/TavallStudios/tavall-database/commit/cc0cd3dbad708ca61805f251263a95e71280b3b7) | Redis configuration surface expanded; service behavior was not tested here. |
| 2026-08-12 8:32 PM PDT | `IN_PROGRESS` | Added atomic typed PostgreSQL entity operations. | [`62024ca8057d`](https://github.com/TavallStudios/tavall-database/commit/62024ca8057d8c4154327f3092e87eee43712731) | PostgreSQL source history records atomic operations; no database run is claimed. |
| 2026-09-22 3:52 AM PDT | `IN_PROGRESS` | Required explicit JPA entity package configuration. | [`eb8d2e465e07`](https://github.com/TavallStudios/tavall-database/commit/eb8d2e465e0715abb29e15f840d228e9bb1cbdeb) | Entity discovery now requires an explicit package boundary; regression tests were not run in this rollout. |
| 2026-09-22 5:15 AM PDT | `IN_PROGRESS` | Added a trusted PostgreSQL statement boundary. | [`ac40359d734f`](https://github.com/TavallStudios/tavall-database/commit/ac40359d734f18474fd444881d0b20e6b862374e) | SQL trust boundary changed in source history; no database acceptance is implied. |
| 2026-09-22 6:26 AM PDT | `IN_PROGRESS` | Avoided invalid Hibernate JNDI registration. | [`05bf86389119`](https://github.com/TavallStudios/tavall-database/commit/05bf86389119982511cfeb2b924d12f08faa0f8b) | Hibernate registration behavior changed; runtime/database verification remains outstanding. |
| `2026-10-09 2:13 PM PDT` | `VALIDATED` | Split Redis into a client-free API module and a Jedis provider module. The API owns the Redis contracts, typed leases and fenced records, and the provider SPI; the provider supplies the Jedis implementation and the `IJedis*` compatibility boundary. | [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124) on `working/redis-api-module-20261009`; affected: `tavall-database-redis-api`, `tavall-database-redis`, `tavall-database-core`, `tavall-database-test-suite` | Validated locally per the commit: `check` passed and `RedisDatabaseContractTest` passed 8/8. Local only: not pushed, not published to GitHub Packages, and no consumer has migrated; Tavall Cloud and Tavall MC remain on raw-Jedis 1.0.0 usage. |

| 2026-10-19 2:34 PM PDT | IN_PROGRESS | Reconciled advisory-lock PR #29 with current main and ran the PostgreSQL provider check on the merged-source candidate. | PR #29 and current main bdc6778 | 17 tests total (12 passed, 5 skipped), 0 failures/errors; all 5 skipped tests require PostgreSQL service because no test JDBC URL is configured; main merge and package/runtime acceptance remain separate. |
| 2026-10-19 3:31 PM PDT | MERGED | Merged advisory-lock PR #29 into main with an ordinary two-parent commit while the PR remained Draft. | Merge `e0936fdbeaf9034830bd72d4e324e9a2c45218bd`, parents `bdc67789723462ba435c5953035a6b632e8c44bc` and `fe9519a8101da2aaefefdab41ae23fe3538b7c16`, tree `4588b90b95160f6066247782466a6441ec7db649`. | The exact PR source tree is now on main. The local provider check passed; Bot status and PostgreSQL/package acceptance were not emitted or run. |

## Validation State

| Validation | State | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Module map and build boundaries | Audited against current main | Main e0936fd and the eight-project Gradle reactor. | Reconcile future module changes against the owning system progression. |
| PostgreSQL provider check | Passed locally on the exact PR #29 source tree now merged | `:tavall-database-postgres:check --rerun-tasks` on `fe9519a`; source tree `4588b90` matches main merge `e0936fd`: 17 tests total (12 passed, 5 skipped), 0 failures/errors. | Run PostgreSQL service scenarios through the owning test path. |
| Tavall Bot status | Not emitted | Exact PR head `fe9519a` returned no status contexts, no review submissions, and no review threads. | Preserve the unavailable Bot aggregate as evidence; the authorized source merge completed using the local provider check. |
| PostgreSQL contention and rollback | Not run | No PostgreSQL service was contacted. | Run against an explicitly disposable service when its test JDBC URL is configured. |
| Consumer source composition | Partial | Tavall-MC PR #333 uses acquireAdvisoryTransactionLock in one atomic operation; earlier focused MC tests are branch-bound evidence. | Rerun against the current exact producer/consumer source set. |
| Package-backed consumer | Not run | No immutable provider artifact was selected for the consumer. | Publish/select an immutable artifact through Tavall CI and validate without source substitution. |
| Module CI | Missing on main | PR #17 targets persistent staging/platform. | Continue that existing source-owned CI transition separately. |

## Acceptance Still Open

| Acceptance | Impact | Next action |
| --- | --- | --- |
| PostgreSQL service integration | Contention and rollback/release are not proven against PostgreSQL. | Run on an explicitly disposable service when available. |
| Immutable package consumer | Source composition does not prove published artifact resolution. | Publish/select the exact artifact through Tavall CI and validate the consumer without source substitution. |
| Main module CI | No .tavallci definition is on main; PR #17 targets staging. | Continue the existing CI transition through its source-owned checks. |

## Next Slice

Run PostgreSQL contention/rollback on a disposable database and validate immutable package-backed consumption through Tavall CI. Continue the existing PR #17 CI transition on its staging target.

<details>
<summary>Documentation Update State</summary>

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | PRIMARY | TavallStudios/tavall-database/docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md on main | 2026-10-19 3:36 PM PDT | PR #29 merge `e0936fd`; local provider check and two-parent ancestry recorded in this progression. |
| Notion | SYNCED | [Tavall Database — SYSTEM PROGRESSION](https://app.notion.com/p/3ef38458ddfd81d0b3ebded692a9cd68) | 2026-10-19 3:36 PM PDT | Refreshed from merged main; post-update fetch verifies PR #29 merge commit and current eight-project map. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 5:59 PM PDT | GitHub | `CREATED` | `docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md` | — | PR [#26](https://github.com/TavallStudios/tavall-database/pull/26); audited main `ec7672bc435872c999e6955c34ca90dab35bc9c4`. | Created system Progression from current module, build, and source-history evidence. |
| 2026-10-09 2:13 PM PDT | GitHub | `UPDATED` | `docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md` | Same path | Commit [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124) on `working/redis-api-module-20261009` | Added the Redis API module, the Redis provider role, the eight-project count, and the 2026-10-09 system timeline entry. |

| 2026-10-19 2:34 PM PDT | GitHub | UPDATED | docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md | Same path | PR #29 main synchronization; local provider check result | Reconciled the system map to main bdc6778 and the Redis API/provider split. |
| 2026-10-19 2:51 PM PDT | Notion | UPDATED | https://app.notion.com/p/3ef38458ddfd81d0b3ebded692a9cd68 | Same page | Post-update fetch after refreshing from GitHub source. | Verified the eight-project system map, current main SHA, and provider test evidence. |
| 2026-10-19 3:31 PM PDT | GitHub | UPDATED | `docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md` | Same path | PR #29 merged at `e0936fd`; two-parent ancestry and identical source tree confirmed. | Updated current main and PR context after the owner-path merge. |
| 2026-10-19 3:36 PM PDT | GitHub + Notion | `SYNCED` | `docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md` ↔ [Tavall Database — SYSTEM PROGRESSION](https://app.notion.com/p/3ef38458ddfd81d0b3ebded692a9cd68) | 2026-10-19 3:32 PM PDT | PR #29 merge `e0936fd`; Notion post-update fetch verifies merged main and system module map. | Reconciled the source and mirror to the completed two-parent merge. |
</details>
