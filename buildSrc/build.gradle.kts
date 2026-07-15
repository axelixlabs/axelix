plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
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
            implementationClass = "com.axelixlabs.plugin.autoconfig.generator.AxelixAutoConfigPlugin"
            displayName = "Axelix Auto-Configuration Plugin"
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    main {
        java {
            srcDirs("src/main/java")
        }
        kotlin {
            srcDirs("src/main/kotlin")
        }
    }
}