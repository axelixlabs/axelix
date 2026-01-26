plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}


gradlePlugin {
    plugins {
        register("axelix-internal") {
            id = "com.axelixlabs.axelix-internal"
            implementationClass = "binary.AxelixPropertiesPlugin"
        }
    }
}