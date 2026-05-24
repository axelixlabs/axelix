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
    id("signing")
    id("net.ltgt.errorprone") version "4.2.0"
    id("test-report-aggregation")
}

allprojects {
    group = "com.axelixlabs"
    version = project.findProperty("axelixVersion")!!

    repositories {
        mavenCentral()
    }
}

dependencies {
    subprojects.forEach {
        testReportAggregation(it)
    }
}

val aggregateTestProfilerReports by tasks.registering(Copy::class) {
    group = "reporting"
    description = "Aggregates spring-test-profiler HTML reports from all subprojects."

    subprojects.forEach { sub ->
        dependsOn(sub.tasks.withType<Test>())
        from(sub.layout.buildDirectory.dir("spring-test-profiler")) {
            into(sub.name)
        }
    }
    into(layout.buildDirectory.dir("reports/spring-test-profiler"))
}

tasks {
    check {
        dependsOn(testAggregateTestReport)
        dependsOn(aggregateTestProfilerReports)
    }
}

subprojects {

    apply(plugin = "java")
    apply(plugin = "com.diffplug.spotless")
    apply(plugin = "pmd")
    apply(plugin = "net.ltgt.errorprone")

    dependencies {
        errorprone("com.google.errorprone:error_prone_core:2.41.0")
        errorprone("com.uber.nullaway:nullaway:0.12.9")
    }

    spotless {
        java {
            palantirJavaFormat("2.87.0")
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
            forbidWildcardImports()
            trimTrailingWhitespace()
//            TODO:
//             For some reason, toggling comments like spotless:off / spotless:on
//             stopped working, disabled it for now
//            toggleOffOn()
            // TODO: removeUnusedImports may not always work https://github.com/diffplug/spotless/issues/2850
            removeUnusedImports("cleanthat-javaparser-unnecessaryimport")

            licenseHeader(
                Paths
                    .get("${rootDir.path}/LICENSE_HEADER")
                    .readText(charset = StandardCharsets.UTF_8)
            )
        }

        kotlin {
            target("src/**/*.kt")
            trimTrailingWhitespace()
            // TODO:
            //  I would like to integrate detekt into spotless
            //  but spotless does not support it as of now https://github.com/diffplug/spotless/issues/143

            licenseHeader(
                Paths
                    .get("${rootDir.path}/LICENSE_HEADER")
                    .readText(charset = StandardCharsets.UTF_8)
            )
        }
    }

    testing {
        suites {
            withType<JvmTestSuite>().configureEach {
                useJUnitJupiter()
            }
        }
    }

    pmd {
        isIgnoreFailures = false
        isConsoleOutput = true
        toolVersion = "7.16.0"
        ruleSetFiles = files("${rootDir}/pmd.ruleset.xml")
    }

    tasks {
        // Enable custom Javadoc tags
        withType(Javadoc::class.java).configureEach {
            val options = options as StandardJavadocDocletOptions
            options.tags(
                "apiNote:a:API Note:",
                "implNote:a:Implementation Note:"
            )
        }

        withType(JavaCompile::class.java).configureEach {
            options.errorprone {
                // TODO Consider enable compilation warnings on first milestone release
                disableAllChecks = true
            }
        }

        named<JavaCompile>("compileJava") {
            options.errorprone {
                check("NullAway", CheckSeverity.ERROR)
                option("NullAway:AnnotatedPackages", "com.axelixlabs.axelix")
                option("NullAway:JSpecifyMode", true)
                option("NullAway:CheckOptionalEmptiness", true)
            }
        }

        check {
            dependsOn(pmdMain, pmdTest)
        }
    }
}

val publishableProjects = listOf(
    project(":sbs:axelix-spring-boot-2-starter"),
    project(":sbs:axelix-spring-boot-3-starter"),
    // TODO: Uncomment when axelix-spring-boot-4-starter is ready
    //project(":sbs:axelix-spring-boot-4-starter"),
    project(":master")
)

configure(publishableProjects) {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        repositories {

            val nexusUrl = project.findProperty("nexus.url") as String? ?: System.getenv("NEXUS_URL")

            // It may be null in case of launches in the PRs
            if (!nexusUrl.isNullOrBlank()) {
                maven {
                    name = "NexusAxelix"
                    url = uri(nexusUrl)
                    credentials {
                        username = project.findProperty("nexus.user") as String? ?: System.getenv("NEXUS_USER")
                        password = project.findProperty("nexus.password") as String? ?: System.getenv("NEXUS_PASSWORD")
                    }
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

            // We do not publish bootJar to MavenCentral
            // TODO: revisit later
//            if (project.name != "master") {
//                maven {
//                    name = "MavenCentral"
//                    url = uri("https://sonatype.com")
//                    credentials {
//                        username = project.findProperty("sonatype.user") as String? ?: System.getenv("SONATYPE_USER")
//                        password = project.findProperty("sonatype.password") as String? ?: System.getenv("SONATYPE_PASSWORD")
//                    }
//                }
//            }
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

    signing {
        // Signing artifacts only in case publishGprPublicationToGitHubPackagesRepository is present
        if (gradle.taskGraph.hasTask(":publishGprPublicationToGitHubPackagesRepository")) {

            val gpgPassphraseEnvVariableName = "PRODUCTION_GPG_SECRET_KEY_PASSPHRASE"
            val gpgSigningKeyIdEnvVariableName = "PRODUCTION_GPG_SECRET_KEY"

            val signingKey = System.getenv(gpgSigningKeyIdEnvVariableName)
            val signingPassword = System.getenv(gpgPassphraseEnvVariableName)

            if (signingKey != null && signingPassword != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(publishing.publications["gpr"])
            } else {
                throw GradleException(
                    """
                    Signing requires:
                    1. $gpgSigningKeyIdEnvVariableName env var.
                    2. $gpgPassphraseEnvVariableName env var.
                    """
                )
            }
        }
    }
}
