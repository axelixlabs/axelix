plugins {
    id("common")
}

dependencies {
    api(project(":common:domain"))

    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}