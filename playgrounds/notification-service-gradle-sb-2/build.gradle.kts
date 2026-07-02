import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STANDARD_ERROR

plugins {
    kotlin("jvm") version "2.2.20"
    kotlin("plugin.spring") version "2.2.20"
    id("org.springframework.boot") version "2.7.18"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.gorylenko.gradle-git-properties") version "4.0.1"
    id("com.diffplug.spotless") version "8.0.0"
    id("org.cyclonedx.bom") version "2.3.1"
}

group = "com.sivalabs.ft"
version = "0.0.2-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
val javaVersionString =
    JavaVersion
        .toVersion(
            java.toolchain.languageVersion
                .get()
                .asInt(),
        ).toString()

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
}

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

extra["springCloudVersion"] = "2021.0.9"
extra["testcontainers.version"] = "1.20.4"

dependencies {
    implementation("com.axelixlabs:axelix-spring-boot-2-starter:1.0.0-M3-SNAPSHOT") {
        isChanging = true
    }
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-web")
    // implementation("org.springframework.cloud:spring-cloud-starter-config")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.kafka:spring-kafka")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.testcontainers:junit-jupiter:${property("testcontainers.version")}")
    testImplementation("org.testcontainers:kafka:${property("testcontainers.version")}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.bootJar {
    archiveFileName = "notification-service-0.0.2-SNAPSHOT.jar"
}

springBoot {
    buildInfo {
        properties {
            additional =
                mapOf(
                    "encoding.source" to "UTF-8",
                    "encoding.reporting" to "UTF-8",
                    "java.source" to javaVersionString,
                    "java.target" to javaVersionString,
                )
        }
    }
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()

    testLogging {
        events(FAILED, STANDARD_ERROR, SKIPPED)
        exceptionFormat = FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
}

gitProperties {
    failOnNoGitDirectory = false
}

spotless {
    kotlin {
        trimTrailingWhitespace()
        leadingTabsToSpaces(4)
        endWithNewline()
        ktlint("1.4.1")
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktlint("1.4.1")
    }
}
