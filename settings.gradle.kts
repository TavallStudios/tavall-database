pluginManagement {
    repositories {
        // Tavall internal artifact repository (same convention as Tavall Cloud): an explicit URL wins, and the
        // development host's internal repository is the default only where it exists.
        val tavallInternalRepository = providers.gradleProperty("TAVALL_INTERNAL_PRIVATE_REPOSITORY_URL")
            .orElse(providers.environmentVariable("TAVALL_INTERNAL_PRIVATE_REPOSITORY_URL"))
            .orNull
            ?.takeIf(String::isNotBlank)
            ?: "/srv/dev-storage/deps/private/snapshots".takeIf { file(it).isDirectory }
        if (tavallInternalRepository != null) {
            maven {
                name = "TavallInternalPrivate"
                url = if (tavallInternalRepository.contains("://")) uri(tavallInternalRepository) else uri(file(tavallInternalRepository))
                content { includeGroupByRegex("org\\.tavall(?:\\..*)?") }
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
