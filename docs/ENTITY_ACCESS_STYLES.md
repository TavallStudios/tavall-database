# Tavall Database Entity Access Styles

## Purpose

This guide ranks how application code reads and writes durable PostgreSQL state through Tavall Database. It is the consumer guide for the entity persistence contract in `tavall-database-postgres`.

The architecture rule lives in [tavall-docs `ENTITY_PERSISTENCE.md`](https://github.com/TavallStudios/tavall-docs/blob/main/docs/quality/code-architecture/ENTITY_PERSISTENCE.md): Tavall Database owns the persistence runtime (entity-manager lifecycle, transactions, discovery); application code models entities and uses the installed entity contract. Because tavall-docs deliberately does not name a concrete accessor, the accessor names below belong to this repository and change with it.

The core rule:

> Application code reaches durable state through `IPostgresDatabase.entities()` (`IPostgresEntityStore`) with mapped entities and named queries. It never owns an `EntityManager`, a transaction callback, or a `*Repository` layer.

---

## The Contract

`IPostgresDatabase.entities()` returns `IPostgresEntityStore`:

| Operation | Use |
| --- | --- |
| `isOpen()` | Whether the store can serve operations; check before a read path that must fail explicitly. |
| `find(type, id)` / `find(type, id, lockMode)` | Load one entity by id. |
| `findNamed(type, queryName, parameters, maxResults)` | Run a `@NamedQuery` declared on the entity; `maxResults` `0` means no limit. |
| `findOneNamed(type, queryName, parameters)` | A named query expected to return at most one entity. |
| `save(entity)` / `saveAll(entities)` | Persist or merge in one owned transaction. |
| `delete(entity)` / `deleteById(type, id)` | Remove in one owned transaction. |
| `executeNamedMutation(queryName, parameters)` | A named bulk update/delete. |
| `executeAtomic(operation)` | Several entity operations in one Tavall Database-owned write transaction. |

`executeAtomic` passes an `IPostgresEntityOperationContext` with the same typed operations. It exposes no `EntityManager` or transaction control, is bound to the calling thread, and must not be retained after the operation returns.

`IPostgresDatabase.jpa()` is deprecated compatibility access for infrastructure migration only. `connections()` and `queries()` are infrastructure capabilities, not the application persistence path.

Entities are discovered per persistence unit from the packages given to `PostgresDatabaseBuilder.entityPackage(...)` (`IPostgresConfigData.getEntityPackages()`).

---

## Production Ranking Summary

| Rank | Style | Production use |
| --- | --- | --- |
| 1 | Managed DataHandler/Resolver with `DependencyAccess<IPostgresDatabase>`, entity store operations, named queries, entity-owned domain mapping | Default |
| 2 | `executeAtomic` with a typed operation | Multi-entity writes that must commit together |
| 3 | `executeNamedMutation` | Bulk updates/deletes declared as named queries |
| 4 | `jpa()`, `connections()`, `queries()` | Infrastructure and migration code only |
| — | `EntityManager`/JDBC ownership, transaction wrappers, `*Repository` types | Not allowed |

---

# Style 1: Managed Data Owner on the Entity Store

## Production Rank

First.

## Shape

The entity owns table mapping, its named queries, and conversion to/from the domain value:

```java
@Entity
@Table(name = "tavall_organization_membership")
@NamedQuery(
        name = TavallOrganizationMembershipEntity.FIND_BY_ACCOUNT,
        query = "select m from TavallOrganizationMembershipEntity m where m.accountId = :accountId"
)
public class TavallOrganizationMembershipEntity {
    public static final String FIND_BY_ACCOUNT = "TavallOrganizationMembershipEntity.findByAccount";

    public TavallOrganizationMembership toDomain() { ... }

    public static TavallOrganizationMembershipEntity fromDomain(TavallOrganizationMembership membership) { ... }
}
```

The DataHandler that owns the data policy resolves the database through Tavall DI and calls the store:

```java
@DelegatesTo(ITavallOrganizationDataHandler.class)
public final class TavallOrganizationDataHandler
        implements ITavallOrganizationDataHandler, DependencyAccess<IPostgresDatabase> {

    @Override
    public List<TavallOrganizationMembership> findMemberships(UUID accountId) {
        return requireOpenDatabase().entities().findNamed(
                        TavallOrganizationMembershipEntity.class,
                        TavallOrganizationMembershipEntity.FIND_BY_ACCOUNT,
                        Map.of("accountId", accountId.toString()),
                        0
                ).stream()
                .map(TavallOrganizationMembershipEntity::toDomain)
                .toList();
    }

    private IPostgresDatabase requireOpenDatabase() {
        IPostgresDatabase database = getPostgresDatabase();
        if (!database.entities().isOpen()) {
            throw new IllegalStateException("Organization storage is unavailable.");
        }
        return database;
    }

    private IPostgresDatabase getPostgresDatabase() {
        return getInstance();
    }
}
```

## Why It Wins

- Query text lives with the mapping it queries; callers pass a stable query-name constant and typed parameters.
- Tavall Database owns every transaction; the handler owns only its data policy (which query, which mapping, how unavailability surfaces).
- The database is resolved per call through Tavall DI ([tavall-di access styles](https://github.com/TavallStudios/tavall-di/blob/main/docs/DI_ACCESS_STYLES.md)), so a replaced or closed database is observed instead of captured.

## Rules

- The persistence unit that consumers resolve must list every consumed entity package through `entityPackage(...)`. A missing package fails at query time, not at startup; add a test that pins the package list.
- Map entities to domain values at the boundary (`toDomain`/`fromDomain` or an entity builder); do not leak entities past the data owner.
- Check `isOpen()` where a read must fail explicitly (for example a fail-closed authority) rather than return empty.
- Add a DataHandler only when there is real data policy (tavall-docs `HANDLERS.md`); a single call site may use the store directly.

---

# Style 2: Atomic Composed Writes

## Production Rank

Second; use it whenever several entity writes must commit or roll back together.

## Shape

```java
MembershipChange change = getPostgresDatabase().entities().executeAtomic(entities -> {
    TavallOrganizationEntity organization = entities
            .find(TavallOrganizationEntity.class, organizationId, LockModeType.PESSIMISTIC_WRITE)
            .orElseThrow();
    entities.save(TavallOrganizationMembershipEntity.fromDomain(membership));
    return MembershipChange.added(organization.toDomain(), membership);
});
```

## Rules

- Return a typed result from the operation; do not retain the context or pass it to another thread.
- Keep non-persistence side effects (events, cache invalidation, network calls) outside the operation, after it returns.
- When a required multi-entity capability is missing, add it to `tavall-database` rather than rebuilding transaction ownership downstream.

---

# Style 3: Named Bulk Mutations

## Production Rank

Third.

Use `executeNamedMutation` for bulk updates or deletes declared as named queries on an entity. Prefer entity `save`/`delete` for single-row changes so mapping and lifecycle stay on the entity.

---

# Style 4: Infrastructure Access

## Production Rank

Infrastructure and migration code only.

`jpa()` is deprecated compatibility access for migrating infrastructure. `connections()` and `queries()` serve schema bootstrap and infrastructure checks. Application features do not use them for their durable state.

---

# Anti-Patterns

| Anti-pattern | Why it is rejected | Use instead |
| --- | --- | --- |
| Owning an `EntityManager`, `EntityTransaction`, or JDBC transaction | Splits transaction ownership from Tavall Database | Entity store operations, Style 2 for atomic writes |
| A new `*Repository` type or a generic CRUD wrapper | Prohibited name and redundant layer (tavall-docs `ENTITY_PERSISTENCE.md`) | Entities + named queries + a DataHandler when policy exists |
| Query strings built in handlers | Query ownership drifts away from the mapping | `@NamedQuery` on the entity |
| Capturing `IPostgresDatabase` in a field or constructor | Stale instance after replacement; bypasses Tavall DI | `DependencyAccess<IPostgresDatabase>` named getter |
| A second persistence unit for one feature | Parallel discovery and lifecycle (tavall-docs `ENTITY_PERSISTENCE.md`) | Add the entity package to the owning unit |
| Retaining the atomic operation context | Context is thread- and transaction-scoped | Return a typed result |

---

# Test Coverage Needed

- Entity round trip: `fromDomain(value).toDomain()` equals the value.
- Named-query constants match the declared `@NamedQuery` names.
- The persistence unit's entity package list contains every consumed entity package.
- Unavailable/closed store behavior of the data owner (explicit failure or documented fallback).
- Atomic writes: rollback leaves no partial state (integration test against PostgreSQL, as in `PostgresJpaIntegrationTest`).
