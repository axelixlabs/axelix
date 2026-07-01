import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
    // What version of Kotlin are we going to use here...?
    kotlin("jvm") version "2.4.0"
}

val springBootVersion = "3.0.13"
val springCloudVersion = "2022.0.4"

val jsonUnitAssertJVersion = "2.40.1"

dependencies {
    // Self
    compileOnly(project(":sbs:starter-domain"))

    // Impl
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    implementation("org.slf4j:slf4j-api")
    implementation("com.jayway.jsonpath:json-path") // version comes from spring-boot-dependencies

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.github.ben-manes.caffeine:caffeine")
    compileOnly("io.micrometer:micrometer-core")
    compileOnly("org.springframework.boot:spring-boot-starter-log4j2")

    // processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")

    // Test
    testImplementation(project(":sbs:starter-domain"))
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.h2database:h2")
    testImplementation("com.github.ben-manes.caffeine:caffeine")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.1.2")
    testImplementation("com.tngtech.archunit:archunit:1.4.2")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:${jsonUnitAssertJVersion}")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 17
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
                implementation("org.jetbrains.lincheck:lincheck:3.6")
                implementation("org.jetbrains.kotlin:kotlin-stdlib")

                // This is the dependency for the compiled production. Additional Test Suites do not have them
                // in any of their Gradle configurations.
                implementation(project(":sbs:axelix-spring-boot-3-starter"))
                implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
                implementation("org.springframework:spring-context")
            }

            targets {
                all {
                    testTask.configure {

                        // Soft-dependency on the default Test Suite (like basic junit unit/integration tests)
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks {
    withType(KotlinJvmCompile::class).configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

axelix {
    properties.put("version", rootProject.version.toString())
}
