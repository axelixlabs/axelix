plugins {
    id("common")
}

dependencies {
    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}