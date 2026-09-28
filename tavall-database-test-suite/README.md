# tavall-database-test-suite

Owns cross-provider tests for the database contracts, aggregate, and supported concrete integrations.

## Responsibility

### Owns
- Unit/integration verification of the repository's database modules.
- Provider-specific integration tests configured by the build.

### Does Not Own
- Production APIs, providers, or service hosting.
- A runtime service or deployment.

## Repository Structure

tavall-database/
├── [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md)
├── [`tavall-database-postgres`](../tavall-database-postgres/README.md)
├── [`tavall-database-mongo`](../tavall-database-mongo/README.md)
├── [`tavall-database-redis`](../tavall-database-redis/README.md)
├── [`tavall-database-qdrant`](../tavall-database-qdrant/README.md)
├── [`tavall-database-core`](../tavall-database-core/README.md)
└── **[`tavall-database-test-suite`](README.md) ← This Module**

## Relationships

| Module / System | Relationship |
| --- | --- |
| [`tavall-database-core-contracts`](../tavall-database-core-contracts/README.md) | Exercises the module in repository verification. |
| [`tavall-database-postgres`](../tavall-database-postgres/README.md) | Exercises the module in repository verification. |
| [`tavall-database-mongo`](../tavall-database-mongo/README.md) | Exercises the module in repository verification. |
| [`tavall-database-redis`](../tavall-database-redis/README.md) | Exercises the module in repository verification. |
| [`tavall-database-qdrant`](../tavall-database-qdrant/README.md) | Exercises the module in repository verification. |
| [`tavall-database-core`](../tavall-database-core/README.md) | Exercises the module in repository verification. |

## Documentation

| Type | Document | Purpose | Surface |
| --- | --- | --- | --- |
| GENERAL | [Repository README](../README.md) | Public overview and module map. | GitHub |
| Technical | [Contribution guide](../CONTRIBUTING.md) | Repository-specific development and validation. | GitHub |
| Progression | [Tavall Database Test Suite Progression](../docs/progression/TAVALL_DATABASE_TEST_SUITE_PROGRESSION.md) | Module implementation, validation, and history. | GitHub |
| Progression | [Tavall Database System Progression](../docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md) | Cross-module architecture and system acceptance. | GitHub |

## Deployment

> This module is not independently deployed.

Runtime owner: `None`. No Deployment record applies to this library or test-only boundary.

## Development

- **Module Type:** `TEST_SUITE`
- **Runtime:** `None`
- **Current PR Stack:** [CI transition #17](https://github.com/TavallStudios/tavall-database/pull/17); documentation update: [PR #26](https://github.com/TavallStudios/tavall-database/pull/26).
- Repository-specific development guide: [CONTRIBUTING.md](../CONTRIBUTING.md).

- **Progression:** [Module Progression](../docs/progression/TAVALL_DATABASE_TEST_SUITE_PROGRESSION.md) · [System Progression](../docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md).
- **Module CI:** Missing in audited main: `.tavallci/ci.yaml`.

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/tavall-database/tavall-database-test-suite/README.md` | 2026-09-27 5:59 PM PDT | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:59 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:59 PM PDT | GitHub | `CREATED` | `TavallStudios/tavall-database/tavall-database-test-suite/README.md` | — | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Added a contextual module README with source-backed ownership and routing. |
| 2026-09-27 5:59 PM PDT | GitHub | `UPDATED` | `TavallStudios/tavall-database/tavall-database-test-suite/README.md` | `TavallStudios/tavall-database/tavall-database-test-suite/README.md` | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Added module and System Progression routes and recorded module CI state. |

</details>
