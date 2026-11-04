import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("common")
    id("java-test-fixtures")
    id("contracts")
}

val jsonUnitAssertJVersion = "2.40.1"

dependencies {
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:${jsonUnitAssertJVersion}")

    // Test
    // Required for `testImplementation` dependencies to pick a version from.
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

contracts {
    modelBasePackage.set("com.axelixlabs.axelix.common.auth.contract")
}

// The generated token classes cannot pass NullAway: a required property is non-null under
// JSpecify, yet the generator emits a Jackson-friendly no-arg constructor that leaves its field
// uninitialized.
tasks.named<JavaCompile>("compileJava") {
    options.errorprone {
        option("NullAway:UnannotatedSubPackages", "com.axelixlabs.axelix.common.auth.contract(\\..*)?")
    }
}
