plugins {
    id("common")
}

dependencies {
    // Self
    api(project(":common:domain"))

    // Impl
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.platform:junit-platform-launcher")
}