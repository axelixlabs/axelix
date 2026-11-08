import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("common")
    id("contracts")
}

val jsonUnitAssertJVersion = "2.40.1"

dependencies {
    // Compile: the generated contract classes carry Jackson annotations, which is the only piece
    // of Jackson we allow for these models: the annotations are honored by both Jackson 2 (Spring
    // Boot 2/3 starters) and Jackson 3 (Spring Boot 4 starter), and the compileOnly scope leaks
    // nothing onto the user's classpath.
    compileOnly("com.fasterxml.jackson.core:jackson-annotations:2.13.5")

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
    modelBasePackage.set("com.axelixlabs.axelix.common.contract")
}

// The generated contract classes cannot pass NullAway: a required property is non-null under
// JSpecify, yet the generator emits a Jackson-friendly no-arg constructor that leaves its field
// uninitialized.
tasks.named<JavaCompile>("compileJava") {
    options.errorprone {
        option("NullAway:UnannotatedSubPackages", "com.axelixlabs.axelix.common.contract(\\..*)?")
    }
}
