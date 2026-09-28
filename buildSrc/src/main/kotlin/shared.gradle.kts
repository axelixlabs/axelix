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
    // Every consumer of JwtEncoderService/JwtDecoderService supplies its own Jackson-backed JwtJsonEngine,
    // built on whichever Jackson it already depends on.
    runtimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")

    // Test
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:${jsonwebtokenVersion}")
}
