pluginManagement {
    val architectureTestsBuild = providers.gradleProperty("tavallArchitectureTestsBuild")
        .orElse(providers.environmentVariable("TAVALL_ARCHITECTURE_TESTS_BUILD"))
        .orNull
    if (!architectureTestsBuild.isNullOrBlank()) {
        val buildDirectory = file(architectureTestsBuild).canonicalFile
        require(buildDirectory.resolve("settings.gradle.kts").isFile) {
            "Tavall Architecture Tests composite build is missing settings.gradle.kts at $buildDirectory"
        }
        includeBuild(buildDirectory) {
            name = "tavall-architecture-tests-source"
        }
    }
    repositories {
        val tavallSnapshots = file("/srv/dev-storage/deps/private/snapshots")
        if (tavallSnapshots.isDirectory) {
            maven {
                name = "TavallSnapshots"
                url = uri(tavallSnapshots)
            }
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "tavall-database"

include(
    "tavall-database-core-contracts",
    "tavall-database-postgres",
    "tavall-database-mongo",
    "tavall-database-redis-api",
    "tavall-database-redis",
    "tavall-database-qdrant",
    "tavall-database-core",
    "tavall-database-test-suite",
)
