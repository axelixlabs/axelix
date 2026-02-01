plugins {
    id("common")
}

dependencies {
    // Self
    api(project(":common:domain"))

    // compileOnly
    // We intentionally use Jackson 2.13.5 for Spring Boot 2.7.x and 3.0.x compatibility:
    // Spring Boot 2 relies on Jackson 2.13 at minimum, and Spring Boot 3 relies on Jacksom 2.19
    // at its base. Becuase of the current deisgn we cannot move this dependency out of common.
    // This would lead to over complication that we do not need at least know. Becuase now we support
    // Only Spring Boot 2 and Spring Boot 3, we do not need to worry about Jackson 3 incompatibility.
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.5")

    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}
