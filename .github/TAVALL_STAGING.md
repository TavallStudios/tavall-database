# Tavall Database Staging Root

```text
<!-- tavall-staging:v1 -->
Type: REPOSITORY_INTEGRATION
State: ACTIVE
Branch: staging/platform
Parent: main
Promotion: MANUAL
ChildMergeTarget: staging/platform
```

This branch is the combined integration tree for Tavall Database JPA/entity-store, atomic typed operations, PostgreSQL/Redis boundaries, package publication, migrations, and downstream compatibility. Child merges are integration for combined validation, not production promotion.
