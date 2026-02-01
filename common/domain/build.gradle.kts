plugins {
    id("common")
}

dependencies {
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
