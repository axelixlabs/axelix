import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("sbs")
    id("com.axelixlabs.axelix-internal")
    kotlin("jvm") version "2.2.21"
}

val springBootTestPlatformVersion = "2.7.18"

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
}

dependencies {
    // Self
    api(project(":common:auth"))
    api(project(":common:api"))
    api(project(":common:domain"))
    api(project(":common:utils"))

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}
