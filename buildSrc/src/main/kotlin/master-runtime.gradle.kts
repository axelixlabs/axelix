import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Convention plugin for the Axelix Master runtime modules (the `:master` library and the
 * per-distribution assembly modules built on top of it). It pins the single Java version those
 * modules compile and run against, so the toolchain is declared in exactly one place.
 */

plugins {
    id("shared")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 25
}
