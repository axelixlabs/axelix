plugins {
    id("common")
    id("java-test-fixtures")
}

val jsonUnitAssertJVersion = "2.40.1"

val jacksonDatabindVersion = "2.13.5"

dependencies {
    testFixturesApi("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")

    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:${jsonUnitAssertJVersion}")

    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
