import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    `java`
    `maven-publish`
}

description = "Axelix Maven plugin: wires spring-test-profiler diagnostics into Spring Boot test builds"

val mavenApiVersion = "3.6.3"
val mavenAnnotationsVersion = "3.13.1"

dependencies {
    // Maven runtime supplies these; they must not be bundled into the plugin jar.
    compileOnly("org.apache.maven:maven-plugin-api:$mavenApiVersion")
    compileOnly("org.apache.maven:maven-core:$mavenApiVersion")
    compileOnly("org.apache.maven:maven-model:$mavenApiVersion")
    compileOnly("org.apache.maven:maven-artifact:$mavenApiVersion")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:$mavenAnnotationsVersion")

    // maven-artifact provides ComparableVersion, exercised directly by the version policy tests.
    testImplementation("org.apache.maven:maven-artifact:$mavenApiVersion")
    testImplementation("org.assertj:assertj-core:3.25.3")
}

// Java 8 bytecode floor so the plugin loads inside Maven on the JDKs that Spring Boot 2
// (possibly JDK 8/11) through Spring Boot 4 (JDK 17+) consumers build with.
tasks.named<JavaCompile>("compileJava") {
    options.release.set(8)
}

// Keep the hand-written Maven plugin descriptor version in sync with the project version.
// Only the @axelixVersion@ token is replaced; Maven's own ${...} expressions are left intact.
tasks.processResources {
    val pluginVersion = version.toString()
    inputs.property("pluginVersion", pluginVersion)
    filesMatching("META-INF/maven/plugin.xml") {
        filter<ReplaceTokens>("tokens" to mapOf("axelixVersion" to pluginVersion))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                packaging = "maven-plugin"
            }
        }
    }
}

// Integration tests drive a real Maven against sample Spring Boot projects (see
// src/integrationTest/resources/it). They are opt-in (not wired into `check`): run with
// ./gradlew :plugins:axelix-maven-plugin:integrationTest. Each test aborts (is skipped) when a
// Maven installation cannot be located, so they never break builds in Maven-less environments.
val integrationTestProjectsDir = layout.projectDirectory.dir("src/integrationTest/resources/it")

testing {
    suites {
        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation("org.apache.maven.shared:maven-invoker:3.2.0")
                implementation("org.assertj:assertj-core:3.25.3")
            }
            targets {
                all {
                    testTask.configure {
                        // The sample builds resolve the plugin from the local Maven repository.
                        dependsOn(tasks.named("publishToMavenLocal"))
                        shouldRunAfter(tasks.named("test"))

                        val mavenHome = System.getenv("MAVEN_HOME")
                                ?: System.getenv("M2_HOME")
                                ?: file("${System.getProperty("user.home")}/.sdkman/candidates/maven/current")
                                        .takeIf { it.exists() }
                                        ?.absolutePath
                                ?: ""
                        systemProperty("axelix.it.maven.home", mavenHome)
                        systemProperty("axelix.it.plugin.version", project.version.toString())
                        systemProperty("axelix.it.projects.dir", integrationTestProjectsDir.asFile.absolutePath)
                    }
                }
            }
        }
    }
}
