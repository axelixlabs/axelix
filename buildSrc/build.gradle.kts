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