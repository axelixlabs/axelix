import Dependencies.jspecifyVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
    // What version of Kotlin are we going to use here...?
    kotlin("jvm") version "2.4.10"
}

val springBootVersion = "3.0.13"
val springCloudVersion = "2022.0.4"

val jsonUnitAssertJVersion = "2.40.1"
val lombokVersion = "1.18.48"
val shedLockVersion = "5.16.0"

// Kept out of the starter's runtime dependencies: instrumentation is opt-in via -javaagent.
val profileAgentLibraries by configurations.creating
val profileAgentPackage = "com/axelixlabs/axelix/sbs/spring/core/conditions/profile/**"

configurations.compileOnly {
    extendsFrom(profileAgentLibraries)
}

dependencies {
    profileAgentLibraries("net.bytebuddy:byte-buddy:1.17.8")
    // Self
    compileOnly(project(":sbs:starter-domain"))

    // Impl
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    implementation("org.slf4j:slf4j-api")

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-webflux")
    compileOnly("org.springframework.cloud:spring-cloud-context")
    compileOnly("org.springframework.boot:spring-boot-starter-data-jpa")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.github.ben-manes.caffeine:caffeine")
    compileOnly("io.micrometer:micrometer-core")
    compileOnly("org.springframework.boot:spring-boot-starter-log4j2")

    // processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")

    // Test
    testImplementation(project(":sbs:starter-domain"))
    testImplementation(testFixtures(project(":sbs:starter-domain")))
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.cloud:spring-cloud-starter")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.h2database:h2")
    testImplementation("com.github.ben-manes.caffeine:caffeine")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.2.3")
    testImplementation("com.tngtech.archunit:archunit:1.4.2")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:${jsonUnitAssertJVersion}")
    testCompileOnly("org.projectlombok:lombok:${lombokVersion}")
    testAnnotationProcessor("org.projectlombok:lombok:${lombokVersion}")
    testImplementation("net.javacrumbs.shedlock:shedlock-spring:${shedLockVersion}") {
        exclude(group = "org.springframework")
    }
    testImplementation("org.awaitility:awaitility")
}

val profileAgentJar by tasks.registering(Jar::class) {
    archiveClassifier.set("profile-agent")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(sourceSets.main.get().output) {
        include(profileAgentPackage)
    }
    from(profileAgentLibraries.map { zipTree(it) }) {
        exclude("META-INF/MANIFEST.MF", "META-INF/*.SF", "META-INF/*.RSA", "META-INF/*.DSA")
    }
    manifest.attributes(
        "Premain-Class" to "com.axelixlabs.axelix.sbs.spring.core.conditions.profile.ProfileDecisionAgent",
        "Multi-Release" to "true"
    )
}

tasks.assemble {
    dependsOn(profileAgentJar)
}

// Separate process: the agent must be installed before Spring's condition classes are loaded.
val profileAgentTest by tasks.registering(Test::class) {
    description = "Tests actual Spring profile decisions with the startup agent installed."
    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath
    useJUnitPlatform()
    include("**/ProfileDecisionAgentTest.class")
    dependsOn(profileAgentJar)
    inputs.file(profileAgentJar.flatMap { it.archiveFile }).withPropertyName("profileAgent")
    jvmArgs("-javaagent:${profileAgentJar.get().archiveFile.get().asFile.absolutePath}")
}

tasks.test {
    exclude("**/ProfileDecisionAgentTest.class")
}

tasks.check {
    dependsOn(profileAgentTest)
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
                implementation("org.jetbrains.lincheck:lincheck:3.7")
                implementation("org.jetbrains.kotlin:kotlin-stdlib")
                implementation("org.jspecify:jspecify:${jspecifyVersion}")

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
