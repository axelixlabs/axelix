import Dependencies.springBootVersion

plugins {
    id("common")
}

dependencies {
    // Self
    api(project(":common:domain"))

    // compileOnly
    // We intentionally use Jackson 2.13.5 for Spring Boot 2.7.x and 3.0.x compatibility
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.13.5")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.platform:junit-platform-launcher")
}