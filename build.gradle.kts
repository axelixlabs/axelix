import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import kotlin.io.path.readText

plugins {
    id("java")
    id("maven-publish")
    id("com.diffplug.spotless") version "8.1.0"
    id("pmd")
    id("net.ltgt.errorprone") version "4.2.0"
}

allprojects {
    group = "com.axelixlabs.axelix"
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
                "com.axelixlabs",
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

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "NexusAxelix"
                url = uri(project.findProperty("nexus.url") as String? ?: System.getenv("NEXUS_URL"))
                credentials {
                    username = project.findProperty("nexus.user") as String? ?: System.getenv("NEXUS_USER")
                    password = project.findProperty("nexus.password") as String? ?: System.getenv("NEXUS_PASSWORD")
                }
            }

            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/axelixlabs/axelix")
                credentials {
                    username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                    password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                }
            }
        }

        publications {

            // Publish to Nexus
            register<MavenPublication>("nexus") {
                from(components["java"])
            }

            // Publish to GitHub Package Registry
            register<MavenPublication>("gpr") {

                from(components["java"])

                // Configure the POM file details
                // TODO: Remove all TODOs below after configuring for Maven Central publication
                // TODO: Requirements: https://maven.apache.org/repository/guide-central-repository-upload.html
                pom {
                    name.set(project.name)
                    description = "A unified monitoring solution for Java Spring Boot deployments"
                    url = "https://github.com/axelixlabs/axelix"
                    packaging = "jar"

                    organization {
                        name.set("Axelix Labs")
                        url.set("https://github.com/axelixlabs")
                    }

                    licenses {
                        license {
                            name.set("GNU Lesser General Public License, Version 3.0")
                            url.set("https://www.gnu.org/licenses/lgpl-3.0.en.html")
                            distribution.set("repo")
                        }
                    }

                    scm {
                        url.set("https://github.com/axelixlabs/axelix")
                    }

                    developers {
                        developer {
                            name.set("Mikhail Polivakha")
                            email.set("mikhailpolivakha@gmail.com")
                            organization.set("Axelix Labs")
                            organizationUrl.set("https://github.com/axelixlabs")
                        }
                        developer {
                            name.set("Nikita Kirillov")
                            email.set("kirilloffnikita1@gmail.com")
                            organization.set("Axelix Labs")
                            organizationUrl.set("https://github.com/axelixlabs")
                        }
                        developer {
                            name.set("Ashot Sargsyan")
                            email.set("ashotsargsyan527@gmail.com")
                            organization.set("Axelix Labs")
                            organizationUrl.set("https://github.com/axelixlabs")
                        }
                        developer {
                            name.set("Sergey Cherkasov")
                            email.set("iamcherkasov.job@gmail.com")
                            organization.set("Axelix Labs")
                            organizationUrl.set("https://github.com/axelixlabs")
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

    tasks.named("check") {
        dependsOn("pmdMain", "pmdTest")
    }

    tasks.named<JavaCompile>("compileJava") {
        options.errorprone {
            // TODO Consider enable compilation warnings on first milestone release
            disableAllChecks = true

            check("NullAway", CheckSeverity.ERROR)
            option("NullAway:AnnotatedPackages", "com.axelixlabs.axelix")
            option("NullAway:JSpecifyMode", true)
            option("NullAway:CheckOptionalEmptiness", true)
        }
    }
    tasks.named<JavaCompile>("compileTestJava") { // disable NullAway on test classes
        options.errorprone {
            disableAllChecks = true
        }
    }

    configure<SigningExtension> {
        // Signing artifacts only in case publishGprPublicationToGitHubPackagesRepository is present
        if (gradle.taskGraph.hasTask(":publishGprPublicationToGitHubPackagesRepository")) {

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
