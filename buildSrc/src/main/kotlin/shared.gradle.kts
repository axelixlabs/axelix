import Dependencies.springBootVersion
import Dependencies.springCloudDependenciesVersion
import Dependencies.jsonUnitAssertJVersion
import Dependencies.jspecifyVersion
import Dependencies.instancioVersion
import Dependencies.jsonwebtokenVersion

plugins {
    id("java-library")
}

dependencies {
    // Impl
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudDependenciesVersion}"))
    implementation("org.jspecify:jspecify:${jspecifyVersion}")

    // Api
    api("io.jsonwebtoken:jjwt-api:${jsonwebtokenVersion}")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudDependenciesVersion"))
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$jsonUnitAssertJVersion")
    testImplementation("org.instancio:instancio-core:${instancioVersion}")
    testImplementation("io.jsonwebtoken:jjwt-api:${jsonwebtokenVersion}")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}