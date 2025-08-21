import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("java")
    id("maven-publish")
    id("com.diffplug.spotless") version "7.1.0"
    id("org.asciidoctor.jvm.convert") version "4.0.4"
    id("pmd")
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
    apply(plugin = "pmd")

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

    pmd {
        isIgnoreFailures = false
        isConsoleOutput = true
        toolVersion = "7.16.0"
        ruleSetFiles = files("${rootDir}/pmd.ruleset.xml")
    }

    tasks.named("build") {
        dependsOn(":buildAllDocs")
    }

    tasks.named("check") {
        dependsOn("pmdMain","pmdTest")
    }
}

asciidoctorj {
    modules {
        diagram {
            version("2.3.2")
        }
    }
}

val docsDir = layout.projectDirectory.dir("docs")
val outputDir = layout.buildDirectory.dir("build/docs")

tasks.withType<AsciidoctorTask> {
    attributes(
        mapOf(
            "toc" to "left",        // Positions the table of contents on the left side
            "icons" to "font",      // Uses font-based icons rather than image icons or text
            "imagesdir" to "images" // Default directory for images
        )
    )

    asciidoctorj {
        requires("asciidoctor-diagram")
    }
}

val docTypes = listOf("internal", "shared")

docTypes.forEach { docType ->
    tasks.register<AsciidoctorTask>(docType) {
        description = "Builds docs for $docType"

        sourceDir(docsDir.dir(docType))
        setOutputDir(file("$outputDir"))
    }
}

tasks.register("buildAllDocs") {
    group = "Documentation"
    description = "Builds all AsciiDoc documentation"

    dependsOn(docTypes)
}