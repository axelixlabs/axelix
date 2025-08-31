plugins {
    id("java-library")
}

dependencies {
    implementation("org.jspecify:jspecify:1.0.0")

    // TODO: should be solved when GH-46 is resolved
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.13.4")
    testImplementation("org.assertj:assertj-core:3.27.4")
    testImplementation("org.junit.platform:junit-platform-launcher:1.13.4")
}