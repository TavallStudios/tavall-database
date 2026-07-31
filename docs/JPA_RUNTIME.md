# PostgreSQL JPA Runtime

## Purpose

`tavall-database-postgres` owns the JPA provider, entity-manager factory, transaction boundary, and shutdown lifecycle for PostgreSQL-backed applications.

Application repositories depend on `IPostgresDatabase` and Jakarta Persistence types. They do not construct or close an `EntityManagerFactory`, import Hibernate APIs, or manage transaction begin, commit, rollback, and entity-manager cleanup themselves.

## Construction

Register managed classes explicitly when a module owns a narrow persistence surface:

```java
IPostgresDatabase database = PostgresDatabaseBuilder.create()
        .jdbcUrl(jdbcUrl)
        .username(username)
        .password(password)
        .persistenceUnitName("novus-ffa")
        .entityClasses(
                FFARatingProfileEntity.class,
                FFARatingHistoryEntity.class
        )
        .entityClassLoader(FFARatingProfileEntity.class.getClassLoader())
        .generateSchema(false)
        .showSql(false)
        .build()
        .orElseThrow();
```

A module may register an entity package instead:

```java
.entityPackage("org.tavall.ffa.persistence.entity")
.entityClassLoader(FFARatingProfileEntity.class.getClassLoader())
```

Package scanning accepts classes annotated with `@Entity`, `@Embeddable`, `@MappedSuperclass`, or `@Converter`. JPA construction fails when configured packages and explicit classes produce no managed persistence types.

Explicit class registration is preferred for small or security-sensitive module boundaries because the complete persistence surface remains visible at composition time.

## Operations

Value-returning reads and writes use `read` and `write`:

```java
Optional<FFARatingProfileEntity> profile = database.jpa().read(entityManager ->
        Optional.ofNullable(entityManager.find(FFARatingProfileEntity.class, playerId))
);

FFARatingProfileEntity saved = database.jpa().write(entityManager -> {
    FFARatingProfileEntity merged = entityManager.merge(profileEntity);
    return merged;
});
```

Void operations use the unambiguous `readVoid` and `writeVoid` forms:

```java
database.jpa().writeVoid(entityManager ->
        entityManager.remove(entityManager.getReference(FFARatingProfileEntity.class, playerId))
);
```

Each operation owns one entity manager and one JPA transaction. A successful write flushes and commits. A successful read commits without an explicit flush. Failures roll back when the transaction is active. Caller exceptions are preserved, while rollback or cleanup failures are attached as suppressed exceptions.

## Read-only databases

`readOnly(true)` rejects `write` and `writeVoid` before an entity manager is opened. Reads remain available.

## JDBC-only compatibility

A PostgreSQL database built without entity classes or entity packages remains a JDBC-only database:

```java
IPostgresDatabase database = PostgresDatabaseBuilder.create()
        .jdbcUrl(jdbcUrl)
        .build()
        .orElseThrow();

boolean configured = database.jpa().isConfigured(); // false
```

Calling a JPA operation on an unconfigured database fails explicitly. Existing `connections()` and `queries()` behavior remains available.

## Lifecycle

The `IPostgresDatabase` instance is the authoritative owner:

1. JPA operations stop accepting work after close.
2. The entity-manager factory closes.
3. Provider infrastructure closes.
4. PostgreSQL connection infrastructure closes.

Applications close the database once. Repositories and module consumers never close shared provider state independently.

For reloadable modules, build the database with the owning generation's classloader and register the resulting database as a module-scoped `AutoCloseable`. A generation must be disabled and drained before its database is closed and its classloader is released.

## Provider boundary

Hibernate is an internal runtime dependency of `tavall-database-postgres`. Public Tavall Database contracts expose Jakarta Persistence and Tavall-owned types only. First-party JARs remain thin and do not embed Hibernate or other third-party classes.
