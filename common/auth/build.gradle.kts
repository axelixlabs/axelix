plugins {
    id("common")
}

val jsonUnitAssertJVersion = "2.40.1"

dependencies {
    api(project(":common:domain"))

    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:${jsonUnitAssertJVersion}")

    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}