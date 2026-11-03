# Tavall Data Technology Stack
> Domain owner: Tavall Database | Reconciled: 2026-11-03 | Technology inventory

## Providers and selection
| Component | Current source / purpose | Authority rule |
| --- | --- | --- |
| `tavall-database-core-contracts` | Provider-neutral typed database/query contracts | Contract owner, no storage engine implied |
| `tavall-database-postgres` | PostgreSQL entity/JPA implementation | Default durable, queryable, transactional application state |
| PostgreSQL `JSONB` | Flexible structured event/settings/metadata values | Same SQL durability; preserve typed columns for identity, relationships and queries |
| `tavall-database-redis-api` | Client-free distributed state, expiring leases and fenced records | Cross-process state only when domain requires it |
| `tavall-database-redis` | Concrete Jedis-backed provider | Transport implementation; never universal domain authority |
| `tavall-database-mongo` | MongoDB provider | Opt-in for proven consumer-specific data model requirements |
| `tavall-database-qdrant` | Qdrant provider | Semantic/vector workloads; never canonical transactional truth by itself |
| Tavall Cache adapters | Memory, disk, PostgreSQL, Redis, Mongo, Qdrant | Cache backends are disposable by contract, even if physically persistent |

## Defaults and explicit exceptions
- **ACCEPTED DEFAULT:** new application domains requiring durable queries or multi-record transactions use PostgreSQL via Tavall Database entity interfaces. Do not force Cloud live Jobs or single-owner file records into SQL.
- PostgreSQL + JSONB is not a direction to store every account, event and relation in one generic JSON column.
- Redis keys must specify owning domain, schema/version, key prefix, TTL/staleness, fencing/idempotency, lost-key behavior and reconciliation source. Redis may be a domain's intentional live authority, as in Tavall Cloud.
- Durable immutable blobs and single-owner versioned records may belong to **Tavall NFS through Tavall Filesystem**, not a new SQL schema. Some domain workflows intentionally use Google Drive for large media.
- `tavall-database-core` aggregates providers; aggregation is not a requirement that consumer runtimes configure or deploy them all.

## Mutation and verification
Validated write -> commit at the domain's authoritative store -> update/invalidate projections and caches -> typed event/result. If a projection fails, return or record the partial failure and support deterministic retries/reconciliation. Test PostgreSQL transactions, JSONB migrations, Redis loss/fencing and provider outages at the exact consuming boundary.

## Sources
- [Tavall Database modules](https://github.com/TavallStudios/tavall-database/blob/main/README.md)
- [Entity access styles](https://github.com/TavallStudios/tavall-database/blob/main/docs/ENTITY_ACCESS_STYLES.md)
- [Tavall Cache](https://github.com/TavallStudios/tavall-cache/blob/main/README.md)
- [State classification policy](https://github.com/TavallStudios/tavall-docs/blob/main/docs/quality/code-architecture/REGISTRIES_CACHES_AND_REPOSITORIES.md)
- [Global stack](https://github.com/TavallStudios/tavall-docs/blob/main/TECH_STACK.md)
