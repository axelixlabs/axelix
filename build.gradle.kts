plugins {
    id("java")
    id("com.diffplug.spotless") version "7.1.0"
}

allprojects {
    group = "com.nucleon-forge.axile.master"
    version = "1.0.0-SNAPSHOT"

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/Nucleon-Forge/axile-common")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }

        mavenCentral()
    }

    apply(plugin = "java-library")
    apply(plugin = "com.diffplug.spotless")

    dependencies {
        implementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
        implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))
        implementation("org.springframework.boot:spring-boot-starter-web")
        implementation("org.jspecify:jspecify:1.0.0")

        // Test
        testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
        testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))
        testImplementation("org.springframework.boot:spring-boot-starter-test")
    }

    plugins.withType<JavaPlugin> {
        tasks.withType<Test>().configureEach {
            useJUnitPlatform()
        }
    }

    spotless {
        java {
            palantirJavaFormat("2.69.0")
            target("src/**/*.java")
            importOrder(
                "java",
                "javax",
                "jakarta",
                "",
                "org.springframework",
                "com.nucleonforge",
                "\\#"
            )
            removeUnusedImports()
            removeWildcardImports()
            trimTrailingWhitespace()
        }
    }
}