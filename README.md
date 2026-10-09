# Tavall Database

Tavall Database is a modular Java library for shared database contracts and concrete PostgreSQL, MongoDB, Redis, and Qdrant providers.

## Why Tavall Database

- Share builder, configuration, and query contracts across multiple storage providers.
- Choose vendor integrations explicitly through separate provider modules.
- Keep aggregate dependencies and cross-provider verification in dedicated modules.

## Features

- Common database, builder, query-handler, and result-mapper interfaces.
- PostgreSQL support with entity-store and JPA boundaries.
- MongoDB, Redis, and Qdrant provider modules.
- A client-free Redis API with typed operations, expiring leases, and fenced versioned records, separate from the Jedis provider.
- An aggregate consumer module and cross-provider test suite.

## Quick Start

This repository does not document published dependency coordinates. Use JDK 25 and the committed Gradle Wrapper to build and verify the source:

```bash
./gradlew check
```

## How It Works

The contracts module defines common interfaces. Provider modules implement them for each backing system. Redis is split: the client-free [`tavall-database-redis-api`](tavall-database-redis-api/README.md) defines the Redis contracts and provider SPI, and [`tavall-database-redis`](tavall-database-redis/README.md) supplies the Jedis provider. The core module aggregates the current providers for consumers, while the test-suite module verifies combined behavior.

## Module Map

| Module | Module Type | Role | Runtime | Boundary |
| --- | --- | --- | --- | --- |
| [`tavall-database-core-contracts`](tavall-database-core-contracts/README.md) | `API` | Shared database, builder, configuration, query, and result contracts | None | Provider-neutral contracts |
| [`tavall-database-redis-api`](tavall-database-redis-api/README.md) | `API` | Client-free Redis contracts, leases, fenced records, provider SPI | Owning consumer runtime | Redis public API; no concrete client |
| [`tavall-database-redis`](tavall-database-redis/README.md) | `FEATURE` | Jedis-backed Redis provider | None | Secondary role: `PROVIDER` |
| [`tavall-database-postgres`](tavall-database-postgres/README.md) | `PROVIDER` | PostgreSQL and JPA provider | None | Provider implementation |
| [`tavall-database-mongo`](tavall-database-mongo/README.md) | `PROVIDER` | MongoDB provider | None | Provider implementation |
| [`tavall-database-qdrant`](tavall-database-qdrant/README.md) | `PROVIDER` | Qdrant provider | None | Provider implementation |
| [`tavall-database-core`](tavall-database-core/README.md) | `LIBRARY` | Aggregate consumer dependency surface | None | Aggregate |
| [`tavall-database-test-suite`](tavall-database-test-suite/README.md) | `TEST_SUITE` | Cross-provider and Redis contract tests | None | Verification only |

## Dependency Graph

```text
tavall-database-core-contracts
  ├── tavall-database-redis-api
  │     └── tavall-database-redis (Jedis provider)
  ├── tavall-database-postgres
  ├── tavall-database-mongo
  └── tavall-database-qdrant

tavall-database-core ──► core-contracts, redis-api, redis, postgres, mongo, qdrant
tavall-database-test-suite ──► core, redis-api, redis, postgres, mongo, qdrant
```

`tavall-database-redis-api` depends only on `tavall-database-core-contracts`. No concrete Redis client may reach its compile classpath.

## Project Structure

├── [`tavall-database-core-contracts`](tavall-database-core-contracts/README.md)
├── [`tavall-database-postgres`](tavall-database-postgres/README.md)
├── [`tavall-database-mongo`](tavall-database-mongo/README.md)
├── [`tavall-database-redis-api`](tavall-database-redis-api/README.md)
├── [`tavall-database-redis`](tavall-database-redis/README.md)
├── [`tavall-database-qdrant`](tavall-database-qdrant/README.md)
├── [`tavall-database-core`](tavall-database-core/README.md)
└── [`tavall-database-test-suite`](tavall-database-test-suite/README.md)

## Documentation

- [Contribution guide](CONTRIBUTING.md) — repository-specific development and validation.
- [Workflow compatibility pointer](docs/quality/GIT_WORKFLOW.md) — redirects to shared policy.
- [Tavall Docs Git Workflow](https://github.com/TavallStudios/tavall-docs/blob/main/docs/quality/GIT_WORKFLOW.md) — shared contribution and review guidance.
- [Tavall Database System Progression](docs/progression/TAVALL_DATABASE_SYSTEM_PROGRESSION.md) — cross-module architecture, validation, and system history.

## Requirements / Compatibility

- JDK 25 for the configured Gradle toolchain.
- Backing-store/provider setup is selected and configured by consuming applications.

## Building From Source

Run `./gradlew check`. Provider integration tests may require external services.

## Contributing

Open a GitHub pull request and follow the repository [CONTRIBUTING.md](CONTRIBUTING.md).

## License

No license file is currently tracked in this repository. Contact the maintainers before redistributing or reusing the code.

<details>
<summary>Documentation Update State</summary>

### Current Locations

| Surface | Sync State | Location | Last Updated | Evidence |
| --- | --- | --- | --- | --- |
| GitHub | `PRIMARY` | `TavallStudios/tavall-database/README.md` | 2026-09-27 5:59 PM PDT | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) |
| Notion | `NOT_APPLICABLE` | — | 2026-09-27 12:59 PM PDT | README routing surface; no 1:1 twin is assigned. |

### Update History

| Timestamp | Surface | Event | Location | Previous Location | Evidence | Notes |
| --- | --- | --- | --- | --- | --- | --- |
| 2026-09-27 12:59 PM PDT | GitHub | `UPDATED` | `TavallStudios/tavall-database/README.md` | `TavallStudios/tavall-database/README.md` | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Reworked the public root README to route contributors and map the current modules. |
| 2026-09-27 5:59 PM PDT | GitHub | `UPDATED` | `TavallStudios/tavall-database/README.md` | `TavallStudios/tavall-database/README.md` | [PR #26](https://github.com/TavallStudios/tavall-database/pull/26) | Added the system Progression route. |

</details>
