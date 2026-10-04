# tavall-database-postgres Progression

> **Status:** Active progression record  
> **Document Type:** `PROGRESSION`  
> **Progression Scope:** `MODULE`  
> **Module Type:** `PROVIDER`  
> **Owning System:** `Tavall Database`  
> **Owns:** Audited implementation, integration, validation, and historical progression for `tavall-database-postgres`  
> **Does Not Own:** Aggregate system progression, deployment history, product/design rules, or Git workflow policy  
> **Audited Against:** Current main `d637362444fa02ce49f4b74ac52b8f5a8aec3670` plus provider implementation checkpoint `0685f72`
> **Last Reconciled:** `2026-10-04 UTC`

## About

Provides the concrete PostgreSQL implementation of Tavall Database contracts.

This record measures the module’s implementation maturity, API/integration state, compatibility, and test evidence.

## Module Context

| Field | Value |
| --- | --- |
| Repository | [TavallStudios/tavall-database](https://github.com/TavallStudios/tavall-database) |
| Module | `tavall-database-postgres` |
| Module Type | `PROVIDER` |
| Owning System | `Tavall Database` |
| System Progression | [Tavall Database System Progression](TAVALL_DATABASE_SYSTEM_PROGRESSION.md) |
| Runtime Owner | `None` |
| Primary Consumers | Tavall-MC account-link completion is the first candidate consumer; exact-source and package-backed acceptance are pending. |
| Current Branch / PR Stack | CI transition [#17](https://github.com/TavallStudios/tavall-database/pull/17); entity-access guide [#27](https://github.com/TavallStudios/tavall-database/pull/27); advisory-lock producer [#29](https://github.com/TavallStudios/tavall-database/pull/29), OPEN/DRAFT at `2940a21079cfea601232a5afeb17555fbe8afb3b`. |
| Audited Revision | Current main base `d637362444fa02ce49f4b74ac52b8f5a8aec3670`; advisory-lock implementation checkpoint `f199689`. |

## Current Status

| Field | State |
| --- | --- |
| Overall State | `PARTIAL` |
| Current Phase | Typed advisory transaction-lock operation is implemented and locally checked; package publication and MC consumer acceptance remain open. |
| Implementation | 23 tracked production Java source files; responsibility boundary is present in Gradle settings/root build configuration |
| Integration | Declared dependency graph and README relationships reviewed; consumer acceptance not verified |
| Validation | `:tavall-database-postgres:check` passed locally at `f199689`; hosted Tavall CI and PostgreSQL integration remain open. |
| Runtime / Consumer Acceptance | Runtime owner is `None`; the Tavall-MC account-link consumer is still being migrated and has not been accepted. |
| Deployment Verification | `N/A` for this non-runtime module |
| Primary Blocker | Main still lacks the module CI definition in PR #17; no PostgreSQL contention/integration result or package-backed MC consumer result has been run for this capability. |
| Next Slice | Reconcile module CI, migrate MC account-link completion, then prove package-backed consumption and PostgreSQL concurrency behavior. |

## Progression Timeline

| Date / Time | State | Progression | Evidence | Result / Remaining Work |
| --- | --- | --- | --- | --- |
| 2026-05-30 11:07 PM PDT | `HISTORICAL_EVIDENCE` | Add Tavall database reactor | [846dc7326483](https://github.com/TavallStudios/tavall-database/commit/846dc732648381333e4229ea2e3afdd690a83fdc) | Current main has 23 production source source files, 7 `src/test` files, and 0 `src/integrationTest` files; no execution result is implied. |
| 2026-08-12 8:32 PM PDT | `IN_PROGRESS` | Added: Provide atomic typed PostgreSQL entity operations | [62024ca8057d](https://github.com/TavallStudios/tavall-database/commit/62024ca8057d8c4154327f3092e87eee43712731) | Current main has 23 production source source files, 7 `src/test` files, and 0 `src/integrationTest` files; no execution result is implied. |
| 2026-09-22 3:52 AM PDT | `IN_PROGRESS` | fix: require explicit JPA entity packages | [eb8d2e465e07](https://github.com/TavallStudios/tavall-database/commit/eb8d2e465e0715abb29e15f840d228e9bb1cbdeb) | Current main has 23 production source source files, 7 `src/test` files, and 0 `src/integrationTest` files; no execution result is implied. |
| 2026-09-22 5:15 AM PDT | `IN_PROGRESS` | feat: add trusted PostgreSQL statement boundary | [ac40359d734f](https://github.com/TavallStudios/tavall-database/commit/ac40359d734f18474fd444881d0b20e6b862374e) | Current main has 23 production source source files, 7 `src/test` files, and 0 `src/integrationTest` files; no execution result is implied. |
| 2026-09-22 6:26 AM PDT | `IN_PROGRESS` | Avoid invalid Hibernate JNDI registration | [05bf86389119](https://github.com/TavallStudios/tavall-database/commit/05bf86389119982511cfeb2b924d12f08faa0f8b) | Current main has 23 production source source files, 7 `src/test` files, and 0 `src/integrationTest` files; no execution result is implied. |
| 2026-10-04 UTC | `IN_PROGRESS` | Design typed PostgreSQL advisory transaction locks | [Technical Design](../technical-design/POSTGRES_ADVISORY_TRANSACTION_LOCK_TECHNICAL_DESIGN.md) | Existing atomic context remains the owner; the design commit itself is not provider or consumer validation. |
| 2026-10-04 UTC | `IN_PROGRESS` | Add the typed transaction-scoped advisory lock operation | [`0685f72`](https://github.com/TavallStudios/tavall-database/commit/0685f72) | `:tavall-database-postgres:check` passed locally; 12 tests passed and five PostgreSQL-service integration cases were skipped because no test JDBC URL was supplied. |

## Validation State

| Validation | State | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Architecture / module boundary | Audited | `settings.gradle.kts`, root `build.gradle.kts`, source tree, module README at `d637362444fa` | Reconcile future changes against module ownership |
| Unit | Provider module check passed locally | `:tavall-database-postgres:check` at source checkpoint `f199689`; JUnit and H2 ran; private Maven snapshots came from the host repository through a temporary init script. | Re-run on exact PR head through Tavall CI. |
| Integration | PostgreSQL lock contention/rollback not run | No PostgreSQL service was contacted. | Run the declared provider/consumer integration boundary against a disposable database. |
| Consumer / Runtime | Not verified | Tavall-MC account-link completion is the first candidate consumer; its typed migration is pending. | Verify source-composite and package-backed consumption independently. |
| End-to-End | N/A or not established | Current module/runtime documentation; no execution evidence | Record acceptance in the owning system Progression |

### Advisory transaction-lock slice

| Validation | State | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Provider module check | Passed locally | `:tavall-database-postgres:check` passed against source checkpoint `0685f72`: 12 tests passed and 5 PostgreSQL-service integration tests were skipped because `TAVALL_TEST_POSTGRES_JDBC_URL` was not set. Java 25 / Gradle 9.6.1 used `/srv/dev-storage/deps/private/snapshots` via a temporary init script. | Re-run against the exact PR head through Tavall CI when the current module CI path is available; run PostgreSQL integration against an approved disposable database. |
| PostgreSQL contention/rollback integration | Not run | No PostgreSQL service was contacted. | Run concurrent lock and rollback cases against an explicitly disposable test database. |
| Exact-source Tavall-MC consumer | Not run | MC account-link source still awaits migration to the typed atomic operation. | Port product completion to `entities().executeAtomic` and the typed lock API. |
| Package-backed Tavall-MC consumer | Not run | No artifact from this branch was published. | Publish the additive minor version through Tavall CI and validate with source composition disabled. |

## Dependencies and Integration

| Dependency / Consumer | Relationship | State | Evidence |
| --- | --- | --- | --- |
| Root build and module source | Independent Gradle subproject | The current feature branch adds a typed provider operation and tests; no runtime owner is assigned. | [`settings.gradle.kts`](../../settings.gradle.kts), [module source](../../tavall-database-postgres/src) |
| Module dependencies | API dependencies on `core-contracts`, PostgreSQL, and Jakarta Persistence; Hibernate is compile-only/runtime; tests use JUnit and H2. | `:tavall-database-postgres:check` resolved and passed locally through the host private snapshot repository. | [Root build](../../build.gradle.kts) |
| Runtime / first consumer | Tavall-MC account-link handler candidate | Exact-source and package-backed consumer acceptance remain open. | Tavall-MC PR #333 and Account System Progression |

## Blockers

| Blocker | Impact | Resolution |
| --- | --- | --- |
| Module CI is still open in PR #17 | Exact-source provider validation is not represented on current main | Reconcile and run the module definition through Tavall CI |
| PostgreSQL integration and MC consumer acceptance are unrun | Lock contention and transaction rollback behavior are not proven on a PostgreSQL service | Run the integration cases against an explicitly disposable test database and validate the MC source/package consumer |

## Next Slice

1. Reconcile module CI ownership through PR #17 and run the provider task on the exact producer head.
2. Migrate MC account-link completion to `IPostgresEntityOperationContext.acquireAdvisoryTransactionLock` while preserving atomicity and lock ordering.
3. Publish the additive provider API through Tavall CI, then prove package-backed consumption independently from exact-source composites.
4. Run account-link race and rollback cases against an explicitly disposable PostgreSQL database when that test service is available.

## Related Documentation

| Type | Document |
| --- | --- |
| Module README | [`README.md`](../../tavall-database-postgres/README.md) |
| Technical Design | [PostgreSQL Advisory Transaction Lock](../technical-design/POSTGRES_ADVISORY_TRANSACTION_LOCK_TECHNICAL_DESIGN.md) |
| Owning system Progression | [`TAVALL_DATABASE_SYSTEM_PROGRESSION.md`](./TAVALL_DATABASE_SYSTEM_PROGRESSION.md) |
| Build / source | [Root build](../../build.gradle.kts), [module source](../../tavall-database-postgres/src) |
| Deployment | `N/A` — this module is not independently deployed |

## Documentation Update State

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/tavall-database/docs/progression/TAVALL_DATABASE_POSTGRES_PROGRESSION.md` | 2026-10-04 UTC | PR #29 head `2940a21079cfea601232a5afeb17555fbe8afb3b`, based on `main@d637362444fa02ce49f4b74ac52b8f5a8aec3670`; provider check passed locally at `0685f72`. |
| Notion | `SYNC_PENDING` | Required twin not inspected | 2026-10-04 UTC | Update through the canonical Tavall documentation flow after connection preflight. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 5:59 PM PDT | GitHub | `CREATED` | `docs/progression/TAVALL_DATABASE_POSTGRES_PROGRESSION.md` | — | PR [#26](https://github.com/TavallStudios/tavall-database/pull/26); audited main `ec7672bc435872c999e6955c34ca90dab35bc9c4` | Created module Progression from the current main source/build/history and module README. |

</details>
