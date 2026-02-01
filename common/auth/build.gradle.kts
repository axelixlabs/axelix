plugins {
    id("common")
}

dependencies {
    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}