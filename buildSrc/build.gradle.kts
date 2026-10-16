plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    // Used by the contract document validation, see contract/ContractDocuments.kt
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.18.2")

    // Used by the stateful contract validation to read the contracts as of the latest release
    // tag, see contract/ReleaseBaseline.kt
    implementation("org.eclipse.jgit:org.eclipse.jgit:7.1.0.202411261347-r")

    // Applied by the 'contracts' convention plugin, see contracts.gradle.kts
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.25.0")

    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
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

    plugins {
        register("axelixAutoConfig") {
            id = "com.axelixlabs.autoconfig"
            implementationClass = "autoconfig.generator.AxelixAutoConfigPlugin"
            displayName = "Axelix Auto-Configuration Plugin"
        }
    }
}