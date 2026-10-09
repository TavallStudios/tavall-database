plugins {
    id("org.tavall.architecture-tests") version "1.1.0"
}

architectureTests {
    // The "database" module enforces the consumer side of the Tavall Database boundary (no raw JDBC or
    // EntityManager ownership in application code). This repository is that boundary's owner, so it is
    // validated by core/patterns/di here and by each consumer repository with the "database" module.
    modules.set(listOf("core", "patterns", "di"))
    debtFile.set(layout.projectDirectory.file("config/architecture-debt.txt"))
    targetProjects.set(
        listOf(
            ":tavall-database-core-contracts",
            ":tavall-database-postgres",
            ":tavall-database-mongo",
            ":tavall-database-redis-api",
            ":tavall-database-redis",
            ":tavall-database-qdrant",
            ":tavall-database-core",
        ),
    )
}

tasks.named("check") {
    dependsOn(tasks.named("architectureTest"))
}

dependencies {
    // The canonical DI rule inspects Tavall DI annotations; production modules do not depend on Tavall DI.
    "tavallArchitectureTestRuntime"("org.tavall:tavall-di:1.0.0")
}
