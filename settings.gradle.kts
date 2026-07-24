plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "tavall-database"

include(
    "tavall-database-core-contracts",
    "tavall-database-postgres",
    "tavall-database-mongo",
    "tavall-database-redis",
    "tavall-database-qdrant",
    "tavall-database-core",
    "tavall-database-test-suite",
)
