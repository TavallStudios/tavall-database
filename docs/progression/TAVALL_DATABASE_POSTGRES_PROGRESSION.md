# tavall-database-postgres Progression

> **Status:** Active progression record  
> **Document Type:** `PROGRESSION`  
> **Progression Scope:** `MODULE`  
> **Module Type:** `PROVIDER`  
> **Owning System:** `Tavall Database`  
> **Owns:** Audited implementation, integration, validation, and historical progression for `tavall-database-postgres`  
> **Does Not Own:** Aggregate system progression, deployment history, product/design rules, or Git workflow policy  
> **Audited Against:** main `bdc67789723462ba435c5953035a6b632e8c44bc` plus advisory-lock PR #29 source `32aa272861726f6fefec116bd459e5075d336004` and Tavall-MC consumer PR #333
> **Last Reconciled:** `2026-10-19 2:34 PM PDT`

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
| Current Branch / PR Stack | Advisory-lock API was carried by PR #29; CI transition [#17](https://github.com/TavallStudios/tavall-database/pull/17) is a separate staging path. PRs #27 and #30 are merged. |
| Audited Revision | Current main `bdc67789723462ba435c5953035a6b632e8c44bc` plus PR #29 source `32aa272861726f6fefec116bd459e5075d336004`. |

## Current Status

| Field | State |
| --- | --- |
| Overall State | PARTIAL |
| Current Phase | PR #29 adds the typed transaction advisory-lock operation on top of current main; the main-synchronized provider check passes locally. Immutable artifact publication and PostgreSQL service acceptance remain open. |
| Implementation | PostgreSQL entity persistence and the advisory-lock operation remain inside the existing provider boundary; no runtime was added. |
| Integration | Tavall-MC PR #333 is the first source-composed consumer; runtime/package acceptance remains separate. |
| Validation | :tavall-database-postgres:check --rerun-tasks passed on the current merge candidate: 17 tests total (12 passed, 5 skipped), 0 failures/errors; all 5 skipped tests require PostgreSQL service because TAVALL_TEST_POSTGRES_JDBC_URL is unset. |
| Runtime / Consumer Acceptance | Runtime owner is None; no package-backed or live PostgreSQL acceptance is claimed. |
| Deployment Verification | N/A for this non-runtime module. |
| Remaining Acceptance | PostgreSQL contention/rollback, immutable artifact publication, exact-head Bot checks, and package-backed MC consumer acceptance remain unverified. |
| Next Slice | Push the main-synchronized PR #29 head, rerun/retain exact-head evidence, then complete the authorized merge and package/database acceptance work. |

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
| 2026-10-04 UTC | `IN_PROGRESS` | Consume the lock through Tavall-MC account-link completion | [Tavall-MC PR #333](https://github.com/TavallStudios/tavall-mc/pull/333) | Focused MC account-link tests and `:novus-backend:check` passed on the exact-source candidate; three existing PostgreSQL/Redis-gated backend tests were skipped. PostgreSQL lock-contention acceptance remains unrun. |

| 2026-10-19 2:34 PM PDT | IN_PROGRESS | Reconciled PR #29 with current main and reran the provider module check. | Current main bdc6778 plus PR #29 source 32aa272 | 17 tests total (12 passed, 5 skipped), 0 failures/errors; all 5 skipped tests require PostgreSQL service because no test JDBC URL is configured. |


## Validation State

| Validation | State | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Architecture / module boundary | Audited | Current main bdc6778, root settings/build, module map and provider README. | Reconcile future changes against module ownership. |
| Unit | Provider module check passed locally | Main-synchronized PR #29 candidate: :tavall-database-postgres:check --rerun-tasks; 17 tests total (12 passed, 5 skipped), 0 failures/errors; all 5 skipped cases require PostgreSQL service. The local-only init script resolved Tavall Logging from the existing private snapshot path. | Repeat after pushing the final PR head; check Tavall Bot exact-head evidence when emitted. |
| PostgreSQL integration | Not run | TAVALL_TEST_POSTGRES_JDBC_URL is unset; no PostgreSQL service was contacted. | Run against an explicitly disposable database. |
| Consumer source composition | Partial | Tavall-MC PR #333 consumes acquireAdvisoryTransactionLock in one atomic operation; earlier focused source-composition tests are branch-bound evidence. | Rerun the exact producer/consumer source pair. |
| Consumer package resolution | Not run | No immutable artifact from the current provider source was selected. | Publish/select an immutable artifact through Tavall CI and validate without source composition. |
| Consumer runtime | Not run | No Paper/Velocity runtime was started. | Run after package and database acceptance. |
| End-to-End | Not established | No service-backed consumer execution is recorded. | Record acceptance in the owning system Progression. |

### Advisory transaction-lock slice

| Validation | State | Evidence | Remaining Work |
| --- | --- | --- | --- |
| Provider module check | Passed locally on the main-synchronized candidate | :tavall-database-postgres:check --rerun-tasks: 17 tests total (12 passed, 5 skipped), 0 failures/errors; all 5 skipped tests require PostgreSQL service because TAVALL_TEST_POSTGRES_JDBC_URL is unset. Java 25 / Gradle 9.6.1; local-only init script adds the existing private snapshot repository for Tavall Logging. | Repeat on the pushed PR head and retain the actual Bot result if emitted. |
| PostgreSQL contention/rollback | Not run | The gated tests were skipped; no PostgreSQL service was contacted. | Run against an explicitly disposable test database. |
| Tavall-MC source consumer | Passed on the prior exact-source candidate | PR #333 used the advisory lock within one atomic operation; focused AccountLink tests and :novus-backend:check passed, with three PostgreSQL/Redis-gated tests skipped. | Rerun against the current exact producer/consumer pair. |
| Package-backed consumer | Not run | No immutable provider artifact is available for selection. | Publish/select via Tavall CI and validate separately from source composition. |

## Acceptance Still Open

| Acceptance | Impact | Next action |
| --- | --- | --- |
| PostgreSQL integration | Advisory lock contention and rollback/release are not proven against PostgreSQL. | Run the gated tests on an explicitly disposable service. |
| Immutable artifact and package consumer | Source composition does not demonstrate the published provider artifact. | Publish/select through Tavall CI and validate the MC package consumer. |
| Exact-head Bot evidence | No Tavall Bot status has been emitted at the current PR head. | Push the reconciled source head and record the exact Bot outcome. |

## Next Slice

Record the exact PR merge and Bot outcome, then prove PostgreSQL contention/rollback and immutable package-backed MC consumption through the owning CI path.

## Documentation Update State

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | PRIMARY | TavallStudios/tavall-database/docs/progression/TAVALL_DATABASE_POSTGRES_PROGRESSION.md | 2026-10-19 2:34 PM PDT | PR #29 source synchronized with main bdc6778; local provider check total 17, 12 passed, 5 skipped. |
| Notion | TEMPORARY_DRIFT | [tavall-database-postgres — PROGRESSION](https://app.notion.com/p/3ef38458ddfd81d385a0f371aeb289db) | 2026-10-19 2:34 PM PDT | Existing 1:1 twin needs refresh from the updated GitHub progression and exact-source check. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 5:59 PM PDT | GitHub | `CREATED` | `docs/progression/TAVALL_DATABASE_POSTGRES_PROGRESSION.md` | — | PR [#26](https://github.com/TavallStudios/tavall-database/pull/26); audited main `ec7672bc435872c999e6955c34ca90dab35bc9c4` | Created module Progression from the current main source/build/history and module README. |

| 2026-10-19 2:34 PM PDT | GitHub | UPDATED | docs/progression/TAVALL_DATABASE_POSTGRES_PROGRESSION.md | Same path | PR #29 main synchronization and local provider check | Recorded 17 total tests, 12 passed, five PostgreSQL-service tests skipped; no live PostgreSQL or package acceptance is claimed. |
</details>
