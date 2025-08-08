plugins {
    id("java")
    id("maven-publish")
    id("com.diffplug.spotless") version "7.1.0"
}

allprojects {
    group = "com.nucleonforge.axile"
    version = "1.0.O-SNAPSHOT"

    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/Nucleon-Forge/axile")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }

        mavenCentral()
    }
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.spotless")

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

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "NucleonForgeAxile"
                url = uri("https://maven.pkg.github.com/Nucleon-Forge/axile")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {

            // Publish to GitHub Package Registry
            register<MavenPublication>("gpr") {
                from(components["java"])
            }
        }
    }
}
