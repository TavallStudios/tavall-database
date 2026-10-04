# PostgreSQL Advisory Transaction Lock — Technical Design

> **Status:** Draft
> **Document Type:** Technical Design
> **Scope:** MODULE
> **Owner:** Tavall Database, `tavall-database-postgres`
> **Parent system:** Tavall Database
> **Module type:** PROVIDER
> **Runtime:** None
> **Primary consumers:** Tavall product data handlers that need a transaction-scoped PostgreSQL lock before a row exists
> **Supersedes:** None

## 1. Decision Summary

Add `IPostgresEntityOperationContext.acquireAdvisoryTransactionLock(String lockKey)` to the typed operation context already scoped to `IPostgresEntityStore.executeAtomic`. Tavall Database owns the native PostgreSQL statement and its transaction boundary. Consumers provide a stable, domain-derived key and continue to compose typed entity operations in the same atomic callback.

The first consumer is Tavall-MC account-link completion. It must serialize claims for a provider/account pair even when no `player_platform_link` row exists yet. PostgreSQL partial unique indexes remain the final invariant.

## 2. Problem and Objective

Before this capability, the MC account-link flow called `database.jpa().write(...)`, retained an application-owned `EntityManager` callback, and issued `pg_advisory_xact_lock` itself. That preserved cross-process serialization but violated the Tavall Database entity boundary. Tavall-MC PR #333 now consumes the typed operation in its account-link completion candidate.

The current atomic operation context has no typed PostgreSQL advisory-lock operation. Removing the lock or splitting the write into independent entity calls would lose transaction ownership and permit concurrent claims to race before a link row exists.

## 3. Non-Goals

- Expose `EntityManager`, `Connection`, `Query`, SQL text, transaction controls, or a general native-query facility to consumers.
- Add a durable lock table or maintain lock state in the application process.
- Replace database uniqueness constraints with advisory locks.
- Change account-link policy, key composition, lock ordering, or user-facing outcomes.
- Implement a generic lock service across Tavall providers.

## 4. Context and Ownership

`tavall-database-postgres` is the lowest existing boundary that owns the JPA context and PostgreSQL-specific transaction mechanics. The existing `IPostgresEntityOperationContext` already represents one active atomic write, is thread-scoped, and deliberately withholds transaction and EntityManager ownership from consumers.

The lock operation therefore belongs on that provider-specific context. It does not belong in shared multi-provider contracts, Tavall-MC, or an application-side persistence wrapper.

## 5. Consumers and Public Contract

The provider API adds:

```java
void acquireAdvisoryTransactionLock(String lockKey);
```

The method is valid only while the context passed to `executeAtomic` remains active on its owning thread. It rejects null or blank keys, binds the key as a query parameter, and returns only after PostgreSQL has acquired the transaction-scoped lock.

The provider derives the PostgreSQL bigint key with `hashtextextended(CAST(:lockKey AS text), 0)` and passes it to `pg_advisory_xact_lock`. The consumer never supplies SQL and does not receive a query result.

## 6. Boundary Classification

### Chosen boundary

An additive operation on the existing typed PostgreSQL atomic context.

### Evidence / pressure

- Existing `executeAtomic` owns the transaction and confines the operation context to its executing thread.
- Account-link claims need to serialize before an active-link row exists.
- The existing implementation already uses a stable string lock key and sorts multiple keys before acquisition.
- Tavall Docs persistence rules require missing transactional behavior to be added to Tavall Database rather than rebuilt downstream.

### Rejected alternatives

- Keeping `database.jpa().write`: leaves application-owned EntityManager and transaction behavior.
- Issuing two separate entity operations: they do not share one transaction or advisory lock lifetime.
- Relying only on a uniqueness exception: changes deterministic product conflict handling into a race-dependent failure path.
- Adding a local lock map, lock registry, or synchronized block: cannot serialize different MC processes and would not protect the database transaction.
- Exposing generic native SQL: expands the consumer persistence authority beyond the single justified capability.

## 7. Module and Runtime Graph

```text
Tavall-MC PlayerAccountLinkCompletionHandler
  -> IPostgresDatabase.entities().executeAtomic(context -> ...)
     -> context.acquireAdvisoryTransactionLock(stableKey)
        -> tavall-database-postgres issues PostgreSQL transaction lock
  -> typed entity reads and writes in the same atomic callback
  -> PostgreSQL transaction commits or rolls back
```

The provider is a library with runtime owner `None`. The Tavall-MC Paper/Velocity process remains the transaction caller. No separate process, database service, or executor is introduced.

## 8. Source / Package Structure

```text
tavall-database-postgres/
  src/main/java/org/tavall/database/postgres/entity/
    IPostgresEntityOperationContext.java
    PostgresEntityOperationContext.java
  src/test/java/org/tavall/database/postgres/entity/
    PostgresEntityOperationContextTest.java
```

## 9. Core Types and Class Roles

- `IPostgresEntityOperationContext`: typed operations available only inside one Tavall Database-owned atomic write.
- `PostgresEntityOperationContext`: provider implementation that validates scope and executes the parameterized PostgreSQL lock statement.
- `IPostgresEntityStore`: remains the public owner of `executeAtomic`; no new transaction owner is added.

## 10. Typed Contracts

The lock key is a stable identity string produced by the product domain. The provider trims no keys and preserves current hashing semantics; it rejects null and blank keys. Database parameter binding prevents key text from becoming SQL syntax.

The operation is ordered with other operations in the same atomic callback. Consumers needing multiple keys sort them consistently before acquiring locks to avoid application-level lock-order inversions.

## 11. Construction and Dependency Access

The method uses the EntityManager already held by `PostgresEntityOperationContext`. It acquires no new dependency, executor, registry, cache, or lifecycle resource. `ensureUsable()` enforces the existing callback and owning-thread contract before executing the query.

## 12. State, Persistence, Cache, Registry, and Events

The advisory lock is transaction-scoped transient database state. PostgreSQL releases it on commit or rollback. It is neither application state nor durable authority; mapped entities and existing unique indexes remain authoritative.

## 13. Primary Flow

1. The consumer validates inputs and derives stable lock keys from product identities.
2. The consumer enters `executeAtomic`.
3. The provider validates the active context and binds each lock key into its fixed PostgreSQL statement.
4. PostgreSQL acquires the transaction lock.
5. The consumer performs typed entity reads, policy checks, and writes in the same callback.
6. Tavall Database commits or rolls back; PostgreSQL releases the lock with the transaction.

## 14. Failure, Recovery, Security, and Compatibility

- A PostgreSQL lock error propagates through the atomic operation so Tavall Database can roll back the transaction.
- The method does not swallow provider failures or fall back to process-local synchronization.
- Only a bound key value crosses the query boundary; SQL and query construction remain provider-owned.
- Expiration of the atomic context and cross-thread use remain rejected by the existing thread/lifecycle guard.
- This additive API does not assign a floating Maven snapshot in the source PR. Tavall CI's versioning and immutable artifact workflow must select and record the package version, channel, and digest before package-backed consumers can resolve it.
- Product lock-key strings and sorted lock ordering remain byte-for-byte compatible with the current implementation.

## 15. Observability and Operator Surface

The operation adds no standalone operator command or persistent lock record. Existing database query logging/metrics remain the provider's observability surface. The consumer reports domain conflicts through its existing result type.

## 16. Concrete Implementation Example

```java
return database.entities().executeAtomic(entities -> {
    lockKeys.stream().sorted()
            .forEach(entities::acquireAdvisoryTransactionLock);
    return completeAccountLink(entities, request);
});
```

The callback contains typed entity operations only; the consumer does not receive an EntityManager or issue native SQL.

## 17. Test and Acceptance Model

- Provider unit tests reject blank lock keys, verify exact parameter binding, and verify that an invalidated operation context rejects further calls.
- Gated PostgreSQL integration tests invoke the lock through `executeAtomic`, verify same-key serialization, and verify rollback releases the lock.
- Tavall-MC account-link tests verify deterministic key ordering and that link/audit/session writes use the same atomic callback. Focused tests and `:novus-backend:check` passed locally on the exact-source consumer candidate.
- Consumer PostgreSQL integration must race identical provider claims, different external identities, and cross-account claims; verify unique-index invariants and transaction rollback/release.
- Package-backed Tavall-MC consumption must be validated independently from exact-source composite resolution. An ad-hoc local Maven repository is not package-backed acceptance.

No PostgreSQL integration result is inferred from unit or source tests.

## 18. Implementation / PR Graph

1. This Tavall Database PR adds the Technical Design, provider API, provider tests, module and system Progression evidence.
2. Tavall-MC PR #333 consumes the exact Database source and ports account-link completion from local JPA callbacks to `executeAtomic`; source checks passed locally, while hosted Tavall CI remains pending.
3. Tavall Database publishes the additive provider artifact through Tavall CI's immutable artifact workflow after normal producer review.
4. Tavall-MC proves package-backed resolution separately from source-composite resolution and runs PostgreSQL integration only against an explicitly disposable test database.

## 19. Future Separation Triggers

If another provider needs the same semantic capability, design a provider-neutral contract only after its transaction semantics can be stated without PostgreSQL advisory-lock behavior.

## 20. Alternatives Rejected

See the boundary decision in Section 6. The selected operation is smaller than a generic query or transaction extension while retaining database-owned semantics.

## 21. Open Questions

- Which disposable PostgreSQL service is authorized for Tavall CI integration validation?
- Which immutable Tavall CI artifact version/channel/digest should the consumer select for package-backed validation?

## 22. Documentation Relationships

- Tavall Docs `ENTITY_PERSISTENCE.md` owns cross-repository persistence principles.
- Tavall Database Postgres Progression owns implementation and provider test status.
- Tavall-MC Account Progression owns product completion behavior and client/web acceptance.

<details>
<summary>Documentation Update State</summary>

| Surface | Sync State | Location | Evidence |
| --- | --- | --- | --- |
| GitHub | `PRIMARY` | `docs/technical-design/POSTGRES_ADVISORY_TRANSACTION_LOCK_TECHNICAL_DESIGN.md` | Tavall Database PR #29 at `a3a96ee8de03c7c8aac59192c9c5868bda471214`, based on `main@d637362444fa02ce49f4b74ac52b8f5a8aec3670`. |
| Notion | `SYNC_PENDING` | [PostgreSQL Advisory Transaction Lock — TECHNICAL DESIGN](https://app.notion.com/p/3ef38458ddfd817dbd45cafbad104cc0) | Update this 1:1 mirror from the current Git source before marking Git `SYNCED`. |

</details>
