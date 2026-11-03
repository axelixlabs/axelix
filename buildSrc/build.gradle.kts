plugins {
    `kotlin-dsl`
    id("com.diffplug.spotless") version "8.10.4"
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Used by the contract document validation, see contract/ContractDocuments.kt
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.22.4")

    // Used by the stateful contract validation to read the contracts as of the latest release
    // tag, see contract/ReleaseBaseline.kt
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.8.0.202609011348-r")

    // Applied by the 'contracts' convention plugin, see contracts.gradle.kts
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.26.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

spotless {
    kotlin {
        target("src/**/*.kt", "src/**/*.gradle.kts")
        // Precompiled script plugins may start with 'plugins'. Extends the 'LICENSE_HEADER_DELIMITER' constant in spotless
        // so that spotless recognizes the 'plugins' and 'object' delimiters.
        licenseHeaderFile("../LICENSE_HEADER", "(package |@file|import |plugins |object )")
    }
}


gradlePlugin {
    plugins {
        register("axelix-internal") {
            id = "com.axelixlabs.axelix-internal"
            implementationClass = "binary.AxelixPropertiesPlugin"
        }
    }

    plugins {
        register("axelix-nodejs") {
            id = "com.axelixlabs.axelix-nodejs"
            implementationClass = "node.NodeJsBuildPlugin"
        }
    }
}