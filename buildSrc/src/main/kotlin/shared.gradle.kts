import Dependencies.jsonUnitAssertJVersion
import Dependencies.jspecifyVersion
import Dependencies.instancioVersion
import Dependencies.jsonwebtokenVersion

plugins {
    id("java-library")
}

dependencies {
    // Impl
    implementation("org.jspecify:jspecify:${jspecifyVersion}")

    // Api
    api("io.jsonwebtoken:jjwt-api:${jsonwebtokenVersion}")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")

    // Test
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$jsonUnitAssertJVersion")
    testImplementation("org.instancio:instancio-core:${instancioVersion}")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}