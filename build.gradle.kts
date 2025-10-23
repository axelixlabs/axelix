import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.asciidoctor.gradle.jvm.AsciidoctorTask

plugins {
    id("java")
    id("maven-publish")
    id("com.diffplug.spotless") version "7.1.0"

    // TODO:
    //  Migrate to a non-alpha 5 major version of assciidoc plugin.
    //  See https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/564
    id("org.asciidoctor.jvm.convert") version "4.0.4"
    id("pmd")
    id("net.ltgt.errorprone") version "4.2.0"
}

allprojects {
    group = "com.nucleonforge.axile"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "maven-publish")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "pmd")
    apply(plugin = "net.ltgt.errorprone")

    dependencies {
        errorprone("com.google.errorprone:error_prone_core:2.41.0")
        errorprone("com.uber.nullaway:nullaway:0.12.9")
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
            toggleOffOn()
        }
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "NucleonForgeAxile"
                url = uri("https://158.160.69.73:8443/repository/axile-monorepo/")
                credentials {
                    username = project.findProperty("nexus.user") as String? ?: System.getenv("NEXUS_USER")
                    password = project.findProperty("nexus.password") as String? ?: System.getenv("NEXUS_PASSWORD")
                }
            }
        }

        publications {

            // Publish to Nexus
            register<MavenPublication>("nexus") {
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

    // TODO: we need to re-visit when we build the docs I think.
    //  I think we need a separate workflow for deploying it actually
//    tasks.named("build") {
//        dependsOn(":buildAllDocs")
//    }

    tasks.named("check") {
        dependsOn("pmdMain","pmdTest")
    }

    tasks.named<JavaCompile>("compileJava") {
        options.errorprone {
            // TODO Consider enable compilation warnings on first milestone release
            disableAllChecks = true

            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.nucleonforge.axile")
            option("NullAway:JSpecifyMode", true)
            option("NullAway:CheckOptionalEmptiness", true)
        }
    }
    tasks.named<JavaCompile>("compileTestJava") { // disable NullAway on test classes
        options.errorprone {
            disableAllChecks = true
        }
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