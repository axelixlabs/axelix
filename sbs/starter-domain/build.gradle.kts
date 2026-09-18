import net.ltgt.gradle.errorprone.errorprone
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("shared")
    id("java-test-fixtures")
    kotlin("jvm") version "2.4.10"
    id("org.openapi.generator") version "7.25.0"
}

val springBootTestPlatformVersion = "2.7.18"

val jsonUnitAssertJVersion = "2.40.1"

dependencies {
    // Self
    api(project(":common"))

    // Compile: the generated contract classes carry Jackson annotations, which is the only piece
    // of Jackson we allow in this module: the annotations are honored by both Jackson 2 (Spring
    // Boot 2/3 starters) and Jackson 3 (Spring Boot 4 starter), and the compileOnly scope leaks
    // nothing onto the user's classpath.
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.13.5")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework:spring-web")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.2.3")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:${jsonUnitAssertJVersion}")
    testImplementation(testFixtures(project(":common")))

    // Gradle needs it to launch the Junit tests, and, unfortunately, spring-boot-starter-test in 2.x
    // does NOT include the launcher, however, it includes the Junit engine, so, we need the launcher only
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
    options.compilerArgs.add("-parameters")
}

// The wire classes of the Axelix Master <-> starter contracts are generated from the OpenAPI
// documents in :common, so that the yaml document stays the single source of truth of every
// contract. Every document describes exactly one operation and gets its own generation task; the
// model package is derived from the feature directory the document lives in.
val contractSources = fileTree("$rootDir/common/src/main/resources/contract") { include("**/*.yaml") }
    .sortedBy { it.absolutePath }
    .map { document ->
        val featurePackage = document.parentFile.name.replace("-", "")
        val operation = document.nameWithoutExtension
        val outputRoot = layout.buildDirectory.dir("generated/openapi/$operation").get().asFile

        val generate = tasks.register<org.openapitools.generator.gradle.plugin.tasks.GenerateTask>(
            "openApiGenerate" + operation.split('-').joinToString("") { part -> part.replaceFirstChar(Char::uppercase) }) {

            generatorName.set("java")
            library.set("resttemplate")
            inputSpec.set(document.absolutePath)
            outputDir.set(outputRoot.absolutePath)
            modelPackage.set("com.axelixlabs.axelix.sbs.spring.core.contract.$featurePackage")
            globalProperties.set(mapOf("models" to "", "modelDocs" to "false", "modelTests" to "false"))
            configOptions.set(mapOf(
                "hideGenerationTimestamp" to "true",
                "openApiNullable" to "false",
                "serializationLibrary" to "jackson",
                "useBeanValidation" to "false",
                "annotationLibrary" to "none",
                "useJspecify" to "true",
            ))

            // The generator offers no option to suppress the @Generated annotation, so it is
            // dropped right after the generation, freeing the module from a javax.annotation-api
            // dependency.
            val rewriteRoot = outputRoot.resolve("src/main/java")
            doLast {
                rewriteRoot.walkTopDown().filter { it.extension == "java" }.forEach { source ->
                    source.writeText(source.readText()
                        .lineSequence().filterNot { it.startsWith("@javax.annotation.Generated") }.joinToString("\n"))
                }
            }
        }

        // builtBy carries the task dependency to every consumer of the source set, including the
        // sourcesJar of the starters this module is shaded into.
        files(outputRoot.resolve("src/main/java")).builtBy(generate)
    }

sourceSets {
    main {
        java {
            contractSources.forEach { srcDir(it) }
        }
    }
}

// The generated contract classes cannot pass NullAway: a required property is non-null under
// JSpecify, yet the generator emits a Jackson-friendly no-arg constructor that leaves its field
// uninitialized.
tasks.named<JavaCompile>("compileJava") {
    options.errorprone {
        option("NullAway:UnannotatedSubPackages", "com.axelixlabs.axelix.sbs.spring.core.contract(\\..*)?")
    }
}

tasks.withType<Pmd>().configureEach {
    exclude("**/contract/**")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {}

        val concurrencyTestsSuite = "concurrencyTest"

        register<JvmTestSuite>(concurrencyTestsSuite) {

            sources {
                kotlin {
                    setSrcDirs(listOf("src/$concurrencyTestsSuite/kotlin"))
                }
            }

            dependencies {
                implementation("org.jetbrains.lincheck:lincheck:3.7")
                implementation("org.jetbrains.kotlin:kotlin-stdlib")

                // Additional Test Suites do not inherit production dependencies automatically.
                implementation(project(":sbs:starter-domain"))
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks {
    withType(KotlinJvmCompile::class).configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}
