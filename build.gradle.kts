import java.util.zip.ZipFile

plugins {
    base
}

group = "org.tavall"
version = "1.0.0"

val jakartaPersistence = libs.jakarta.persistence
val jacksonDatabind = libs.jackson.databind
val jacksonBom = libs.jackson.bom
val jedis = libs.jedis
val junitJupiter = libs.junit.jupiter
val junitBom = libs.junit.bom
val junitPlatformLauncher = libs.junit.platform.launcher
val mongodbDriver = libs.mongodb.driver
val postgresql = libs.postgresql
val tavallLogging = libs.tavall.logging

subprojects {
    group = rootProject.group
    version = rootProject.version

    apply(plugin = "java-library")
    apply(plugin = "maven-publish")

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(25)
        withSourcesJar()
        withJavadocJar()
    }

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencyLocking {
        lockAllConfigurations()
    }

    dependencies {
        "testImplementation"(platform(junitBom))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    tasks.withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
    }

    val verifyJarContents = tasks.register("verifyJarContents") {
        dependsOn(tasks.named("jar"))
        val archive = tasks.named<Jar>("jar").flatMap { it.archiveFile }
        inputs.file(archive)
        doLast {
            val forbidden = listOf("com/fasterxml/", "com/mongodb/", "org/postgresql/", "redis/clients/")
            ZipFile(archive.get().asFile).use { jar ->
                val embedded = jar.entries().asSequence().map { it.name }
                    .firstOrNull { entry -> forbidden.any(entry::startsWith) }
                check(embedded == null) { "Third-party class embedded in first-party JAR: $embedded" }
            }
        }
    }

    tasks.named("check") {
        dependsOn(verifyJarContents)
    }

    extensions.configure<PublishingExtension> {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifactId = project.name
            }
        }
        repositories {
            val token = providers.environmentVariable("GITHUB_TOKEN")
            if (token.isPresent) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/TavallStudios/tavall-database")
                    credentials {
                        username = providers.environmentVariable("GITHUB_ACTOR").orNull
                        password = token.get()
                    }
                }
            }
        }
    }
}

project(":tavall-database-core-contracts") {
    dependencies {
        "api"(tavallLogging)
    }
}

project(":tavall-database-postgres") {
    dependencies {
        "api"(project(":tavall-database-core-contracts"))
        "api"(postgresql)
        "api"(jakartaPersistence)
    }
}

project(":tavall-database-mongo") {
    dependencies {
        "api"(project(":tavall-database-core-contracts"))
        "api"(mongodbDriver)
        "testImplementation"(junitJupiter)
        "testRuntimeOnly"(junitPlatformLauncher)
    }
}

project(":tavall-database-redis") {
    dependencies {
        "api"(project(":tavall-database-core-contracts"))
        "api"(jedis)
        "testImplementation"(junitJupiter)
        "testRuntimeOnly"(junitPlatformLauncher)
    }
}

project(":tavall-database-qdrant") {
    dependencies {
        "api"(project(":tavall-database-core-contracts"))
        "api"(platform(jacksonBom))
        "api"(jacksonDatabind)
        "testImplementation"(junitJupiter)
        "testRuntimeOnly"(junitPlatformLauncher)
    }
}

project(":tavall-database-core") {
    dependencies {
        "api"(project(":tavall-database-core-contracts"))
        "api"(project(":tavall-database-postgres"))
        "api"(project(":tavall-database-mongo"))
        "api"(project(":tavall-database-redis"))
        "api"(project(":tavall-database-qdrant"))
    }
}

project(":tavall-database-test-suite") {
    dependencies {
        "api"(project(":tavall-database-core"))
        "api"(project(":tavall-database-postgres"))
        "api"(project(":tavall-database-mongo"))
        "api"(project(":tavall-database-redis"))
        "api"(project(":tavall-database-qdrant"))
        "testImplementation"(junitJupiter)
        "testRuntimeOnly"(junitPlatformLauncher)
    }
}
