import Dependencies.springBootVersion
import Dependencies.springCloudDependenciesVersion
import Dependencies.jsonUnitAssertJVersion
import Dependencies.jspecifyVersion
import Dependencies.instancioVersion

plugins {
    id("java-library")
}

dependencies {
    // Imp
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudDependenciesVersion}"))
    implementation("org.jspecify:jspecify:${jspecifyVersion}")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudDependenciesVersion"))
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$jsonUnitAssertJVersion")
    testImplementation("org.instancio:instancio-core:${instancioVersion}")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}