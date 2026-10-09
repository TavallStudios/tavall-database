# tavall-database-redis

Provides the Jedis-backed runtime provider for the client-free [`tavall-database-redis-api`](../tavall-database-redis-api/README.md) contracts.

## Responsibility

### Owns
- The Jedis implementation of `IRedisDatabase`, its builder (`RedisDatabaseBuilder.buildJedis()`), connection handler, query, lease, and versioned-record handlers.
- `JedisRedisDatabaseProvider`, registered under `META-INF/services` for `IRedisDatabaseProvider`.
- Compatibility types for raw-client consumers: `IJedisRedisDatabase` and `IJedisRedisConnectionHandler`.

### Does Not Own
- The provider-neutral Redis contracts, exceptions, `RedisKey`, or the provider SPI. Those belong to [`tavall-database-redis-api`](../tavall-database-redis-api/README.md).
- Domain key layout, payload schemas, or reconciliation policy.
- A standalone database runtime or external Redis service lifecycle.

## Repository Structure

tavall-database/
├── [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md)
├── [`tavall-database-postgres`](../tavall-database-postgres/README.md)
├── [`tavall-database-mongo`](../tavall-database-mongo/README.md)
├── [`tavall-database-redis-api`](../tavall-database-redis-api/README.md)
├── **[`tavall-database-redis`](README.md) ← This Module**
├── [`tavall-database-qdrant`](../tavall-database-qdrant/README.md)
├── [`tavall-database-core`](../tavall-database-core/README.md)
└── [`tavall-database-test-suite`](../tavall-database-test-suite/README.md)

## Relationships

| Module / System | Relationship |
| --- | --- |
| [`tavall-database-redis-api`](../tavall-database-redis-api/README.md) | Implements the Redis API and the `IRedisDatabaseProvider` SPI. Consumers depend on the API and receive this provider at runtime. |
| [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md) | Inherits the shared database contracts through the API module. |
| [`tavall-database-core`](../tavall-database-core/README.md) | Included in the aggregate consumer module. |
| [`tavall-database-test-suite`](../tavall-database-test-suite/README.md) | Covered by `RedisDatabaseContractTest` against a real Redis container. |

## Documentation

| Type | Document | Purpose | Surface |
| --- | --- | --- | --- |
| GENERAL | [Repository README](../README.md) | Public overview and module map. | GitHub |
| Technical | [Contribution guide](../CONTRIBUTING.md) | Repository-specific development and validation. | GitHub |
| API | [Tavall Database Redis API](../tavall-database-redis-api/README.md) | Client-free contracts this provider implements. | GitHub |
| Progression | [Tavall Database Redis Progression](../docs/progression/TAVALL_DATABASE_REDIS_PROGRESSION.md) | Module implementation, validation, and history. | GitHub |
| Progression | [Tavall Database System Progression](../docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md) | Cross-module architecture and system acceptance. | GitHub |

## Deployment

> This module is not independently deployed.

Runtime owner: `None`. Consumers select this provider at their composition boundary. No Deployment record applies to this library or test-only boundary.

## Development

- **Module Type:** `FEATURE`
- **Secondary Role:** `PROVIDER`
- **Runtime:** `None`
- **Current PR Stack:** Local branch `working/redis-api-module-20261009` at [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124), not pushed. Prior stack: [CI transition #17](https://github.com/TavallStudios/tavall-database/pull/17); documentation update: [PR #26](https://github.com/TavallStudios/tavall-database/pull/26).
- Repository-specific development guide: [CONTRIBUTING.md](../CONTRIBUTING.md).

- **Progression:** [Module Progression](../docs/progression/TAVALL_DATABASE_REDIS_PROGRESSION.md) · [System Progression](../docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md).
- **Module CI:** Missing in audited main: `.tavallci/ci.yaml`.

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/tavall-database/tavall-database-redis/README.md` | 2026-09-27 5:59 PM PDT | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:59 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:59 PM PDT | GitHub | `CREATED` | `TavallStudios/tavall-database/tavall-database-redis/README.md` | — | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Added a contextual module README with source-backed ownership and routing. |
| 2026-09-27 5:59 PM PDT | GitHub | `UPDATED` | `TavallStudios/tavall-database/tavall-database-redis/README.md` | `TavallStudios/tavall-database/tavall-database-redis/README.md` | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Added module and System Progression routes and recorded module CI state. |
| 2026-10-09 2:13 PM PDT | GitHub | `UPDATED` | `tavall-database-redis/README.md` | Same path | Commit [`98b9312`](https://github.com/TavallStudios/tavall-database/commit/98b9312c942c192e841e0c26183b64107fb06124) on `working/redis-api-module-20261009` | Reframed the module as the Jedis provider of `tavall-database-redis-api`; recorded the FEATURE type with PROVIDER secondary role. |

</details>
