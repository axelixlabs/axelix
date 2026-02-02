plugins {
    id("shared")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

val jacksonDatabindVersion = "2.13.5"
val springBootTestPlatformVersion = "2.7.18"

dependencies {
    // compileOnly
    // We intentionally use Jackson 2.13.5 for Spring Boot 2.7.x and 3.0.x compatibility:
    // Spring Boot 2 relies on Jackson 2.13 at minimum, and Spring Boot 3 relies on Jacksom 2.19
    // at its base. Because of the current design we cannot move this dependency out of common.
    // This would lead to over complication that we do not need at least know. Because now we support
    // Only Spring Boot 2 and Spring Boot 3, we do not need to worry about Jackson 3 incompatibility.
    compileOnly("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
}