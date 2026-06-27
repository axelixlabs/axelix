import Dependencies.jspecifyVersion
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
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:${jsonwebtokenVersion}")
}
