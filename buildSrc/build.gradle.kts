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