import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.asciidoctor.gradle.jvm.AsciidoctorTask
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.io.path.readText

plugins {
    id("java")
    id("maven-publish")
    id("com.diffplug.spotless") version "8.1.0"

    // TODO:
    //  Migrate to a non-alpha 5 major version of assciidoc plugin.
    //  See https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/564
    id("org.asciidoctor.jvm.convert") version "4.0.4"
    id("pmd")
    id("net.ltgt.errorprone") version "4.2.0"
}

allprojects {
    group = "com.nucleonforge.axelix"
    version = project.findProperty("axelixVersion")!!

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
    apply(plugin = "signing")

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
            forbidWildcardImports()
            trimTrailingWhitespace()
//            TODO:
//             For some reason, toggling comments like spotless:off / spotless:on
//             stopped working, disabled it for now
//            toggleOffOn()

            licenseHeader(
                Paths
                    .get("${rootDir.path}/LICENSE_HEADER")
                    .readText(charset = StandardCharsets.UTF_8)
            )
        }
    }

    val isReleasePipeline = System.getenv("CI") != null && System.getenv("RELEASE_TAG") != null

    if (isReleasePipeline) {
        java {
            withJavadocJar()
            withSourcesJar()
        }
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "NexusAxelix"
                url = uri("https://158.160.69.73:8443/repository/axile-monorepo/")
                credentials {
                    username = project.findProperty("nexus.user") as String? ?: System.getenv("NEXUS_USER")
                    password = project.findProperty("nexus.password") as String? ?: System.getenv("NEXUS_PASSWORD")
                }
            }

            if (isReleasePipeline) {
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/Nucleon-Forge/axelix")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }

        publications {

            // Publish to Nexus
            register<MavenPublication>("nexus") {
                from(components["java"])
            }

            // Publish to GitHub Package Registry
            if (isReleasePipeline) {
                register<MavenPublication>("gpr") {
                    from(components["java"])

                    // Configure the POM file details
                    // TODO: Remove all TODOs below after configuring for Maven Central publication
                    // TODO: Requirements: https://maven.apache.org/repository/guide-central-repository-upload.html
                    pom {
                        name.set(project.name)
                        description = "description" //TODO add description
                        url = "https://www.example.com" //TODO: add valid url
                        packaging = "jar"

                        organization {
                            name.set("Nucleon-Forge")
                            url.set("https://github.com/Nucleon-Forge") //TODO
                        }

                        licenses {
                            license {
                                name.set("Apache License, Version 2.0")
                                url.set("https://www.apache.org/licenses/LICENSE-2.0")
                                distribution.set("repo")
                            }
                        }

                        scm {
                            url.set("https://github.com/Nucleon-Forge/axelix")
                        }

                        developers {
                            developer {
                                name.set("Mikhail Polivakha")
                                email.set("axelix@gmail.com") //TODO
                                organization.set("Nucleon-Forge")
                                organizationUrl.set("https://github.com/Nucleon-Forge")
                            }
                            developer {
                                name.set("Nikita Kirillov")
                                email.set("kirilloffnikita1@gmail.com")
                                organization.set("Nucleon-Forge")
                                organizationUrl.set("https://github.com/Nucleon-Forge")
                            }
                            developer {
                                name.set("Ashot Sargsyan")
                                email.set("axelix@gmail.com") //TODO
                                organization.set("Nucleon-Forge")
                                organizationUrl.set("https://github.com/Nucleon-Forge")
                            }
                            developer {
                                name.set("Sergey Cherkasov")
                                email.set("axelix@gmail.com") //TODO
                                organization.set("Nucleon-Forge")
                                organizationUrl.set("https://github.com/Nucleon-Forge")
                            }
                        }
                    }
                }
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
        dependsOn("pmdMain", "pmdTest")
    }

    tasks.named<JavaCompile>("compileJava") {
        options.errorprone {
            // TODO Consider enable compilation warnings on first milestone release
            disableAllChecks = true

            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.nucleonforge.axelix")
            option("NullAway:JSpecifyMode", true)
            option("NullAway:CheckOptionalEmptiness", true)
        }
    }
    tasks.named<JavaCompile>("compileTestJava") { // disable NullAway on test classes
        options.errorprone {
            disableAllChecks = true
        }
    }

    if (isReleasePipeline) {
        configure<SigningExtension> {
            val signingKey = System.getenv("PGP_SIGNING_KEY")
            val signingPassword = System.getenv("PGP_SIGNING_KEY_PASSPHRASE")

            if (signingKey != null && signingPassword != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(publishing.publications["gpr"])
            } else {
                throw GradleException(
                    """
                Signing requires:
                1. signing.key property OR PGP_SIGNING_KEY env var.
                2. signing.password property OR SIGNING_KEY_PASSPHRASE env var.
                """
                )
            }
        }
    }

    // Enable custom Javadoc tags
    tasks.withType<Javadoc> {
        val options = options as StandardJavadocDocletOptions
        options.tags(
            "apiNote:a:API Note:",
            "implNote:a:Implementation Note:"
        )
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
val outputDirectory = layout.buildDirectory.dir("build/docs")

tasks.withType<AsciidoctorTask> {
    attributes(
        mapOf(
            "source-highlighter" to "rouge",
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
        setOutputDir(file(outputDirectory))
    }
}

tasks.register("buildAllDocs") {
    group = "Documentation"
    description = "Builds all AsciiDoc documentation"

    dependsOn(docTypes)
}