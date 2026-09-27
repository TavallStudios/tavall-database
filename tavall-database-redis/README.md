# tavall-database-redis

Provides the concrete Redis implementation of Tavall Database contracts.

## Responsibility

### Owns
- Redis configuration, builder, connection, and query integration code.
- Provider-specific persistence APIs and exceptions for Redis.

### Does Not Own
- The shared database contract definition or external service lifecycle.
- A standalone database runtime.

## Repository Structure

tavall-database/
├── [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md)
├── [`tavall-database-postgres`](../tavall-database-postgres/README.md)
├── [`tavall-database-mongo`](../tavall-database-mongo/README.md)
├── **[`tavall-database-redis`](README.md) ← This Module**
├── [`tavall-database-qdrant`](../tavall-database-qdrant/README.md)
├── [`tavall-database-core`](../tavall-database-core/README.md)
└── [`tavall-database-test-suite`](../tavall-database-test-suite/README.md)

## Relationships

| Module / System | Relationship |
| --- | --- |
| [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md) | Implements and extends the shared database contracts. |
| [`tavall-database-core`](../tavall-database-core/README.md) | Included in the aggregate consumer module. |
| [`tavall-database-test-suite`](../tavall-database-test-suite/README.md) | Covered by cross-provider verification. |

## Documentation

| Type | Document | Purpose | Surface |
| --- | --- | --- | --- |
| GENERAL | [Repository README](../README.md) | Public overview and module map. | GitHub |
| Technical | [Contribution guide](../CONTRIBUTING.md) | Repository-specific development and validation. | GitHub |

## Deployment

> This module is not independently deployed.

Runtime owner: `None`. No Deployment record applies to this library or test-only boundary.

## Development

- **Module Type:** `PROVIDER`
- **Runtime:** `None`
- **Current PR Stack:** [CI transition #17](https://github.com/TavallStudios/tavall-database/pull/17); documentation update: [PR #26](https://github.com/TavallStudios/tavall-database/pull/26).
- Repository-specific development guide: [CONTRIBUTING.md](../CONTRIBUTING.md).


<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/tavall-database/tavall-database-redis/README.md` | 2026-09-27 12:59 PM PDT | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:59 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:59 PM PDT | GitHub | `CREATED` | `TavallStudios/tavall-database/tavall-database-redis/README.md` | — | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Added a contextual module README with source-backed ownership and routing. |

</details>
