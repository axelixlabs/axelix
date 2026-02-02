plugins {
    id("common")
}

dependencies {
    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.platform:junit-platform-launcher")
}
