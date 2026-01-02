plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}


gradlePlugin {
    plugins {
        register("axelix-internal") {
            id = "com.nucleonforge.axelix-internal"
            implementationClass = "binary.AxelixPropertiesPlugin"
        }
    }
}