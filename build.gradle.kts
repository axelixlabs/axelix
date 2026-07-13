import net.ltgt.gradle.errorprone.CheckSeverity
import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository
import org.gradle.jvm.toolchain.JavaLanguageVersion
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.util.Base64
import kotlin.io.path.readText

plugins {
    id("java")
    id("maven-publish")
    id("com.diffplug.spotless") version "8.8.0"
    id("pmd")
    id("signing")
    id("net.ltgt.errorprone") version "4.4.0"
    id("test-report-aggregation")
}

val projectNamespace = "com.axelixlabs"

allprojects {
    group = projectNamespace
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
        errorprone("com.google.errorprone:error_prone_core:2.50.0")
        errorprone("com.uber.nullaway:nullaway:0.13.7")
    }

    spotless {
        java {
            palantirJavaFormat("2.90.0")
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

// Release compatibility jobs that swap the JVM used to launch starter tests.
// This property is supposed be supplied only during release builds. If it is not there, then
// Spring Boot starters will just use the same java version as it is configured in their toolchain

val starterTestJavaVersionPropertyName = "axelix.starter.tests.java.version"
val starterTestJavaVersion = providers.gradleProperty(starterTestJavaVersionPropertyName).orNull?.toInt()

val starterModules = listOf(
    project(":sbs:axelix-spring-boot-2-starter"),
    project(":sbs:axelix-spring-boot-3-starter")
    // TODO: Uncomment when axelix-spring-boot-4-starter is ready
    // project(":sbs:axelix-spring-boot-4-starter")
)

if (starterTestJavaVersion != null) {
    configure(starterModules) {
        tasks.withType<Test>().configureEach {
            javaLauncher = javaToolchains.launcherFor {
                languageVersion = JavaLanguageVersion.of(starterTestJavaVersion)
            }
        }
    }
}

val commonModules = listOf(
    project(":sbs:starter-domain"),
    project(":common:auth"),
    project(":common:api"),
    project(":common:domain"),
    project(":common:utils")
)

// Apply publishing and signing plugins to all starter modules only
val mavenCentral = "ossrh-staging-api"
val mainPublication = "main"

// We only publish the starters to the maven central
configure(starterModules) {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")

    java {
        withJavadocJar()
        withSourcesJar()
    }

    // Pack shared submodule classes, sources, and docs into a single fat artifact
    tasks {
        jar {
            duplicatesStrategy = DuplicatesStrategy.WARN
            commonModules.forEach { from(it.sourceSets.main.get().output) }
        }
        named<Jar>("sourcesJar") {
            duplicatesStrategy = DuplicatesStrategy.WARN
            commonModules.forEach { from(it.sourceSets.main.get().allSource) }
        }
        withType<Javadoc> {
            commonModules.forEach { source(it.sourceSets.main.get().allJava) }
            classpath = project.configurations.compileClasspath.get()
        }
        named<Jar>("javadocJar") {
            dependsOn(javadoc)
            duplicatesStrategy = DuplicatesStrategy.WARN
            from(javadoc.get().destinationDir)
        }
    }

    publishing {
        repositories {

            maven {
                name = mavenCentral
                url = uri("https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/")
                credentials {
                    username = System.getenv("PRODUCTION_MAVEN_CENTRAL_USERNAME")
                    password = System.getenv("PRODUCTION_MAVEN_CENTRAL_PASSWORD")
                }
            }
        }

        publications {

            // The 'main' publication. Created for each subproject that is supposed to be published.
            register<MavenPublication>(mainPublication) {
                from(components["java"])

                // Configure the POM file details
                pom {
                    name.set(project.name)
                    description = "An AI-native monitoring solution for Java Spring Boot deployments"
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

    val gpgPassphraseEnvVariableName = "PRODUCTION_GPG_SECRET_KEY_PASSPHRASE"
    val gpgSigningKeyIdEnvVariableName = "PRODUCTION_GPG_SECRET_KEY"

    val signingKey = System.getenv(gpgSigningKeyIdEnvVariableName)
    val signingPassword = System.getenv(gpgPassphraseEnvVariableName)

    signing {
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(signingKey, signingPassword)
        }
        sign(publishing.publications.getByName(mainPublication))
    }

    gradle.taskGraph.whenReady {
        val isPublishing = allTasks.any { it is PublishToMavenRepository }

        signing.isRequired = isPublishing

        if (signing.isRequired && (signingKey == null || signingPassword == null)) {
            throw GradleException(
                """
                Signing requires:
                1. $gpgSigningKeyIdEnvVariableName env var.
                2. $gpgPassphraseEnvVariableName env var.
                """.trimIndent()
            )
        }
    }

    // We're doing that as a hack. The problem is that for now, there is no "good" or standard way of publishing maven-like
    // artifacts from gradle build system to maven central (surprise!). They're currently working on it being fixed, but in order
    // to make the standard mavne-publish plugin work, we need to add one more HTTP query to the Maven OSSRH registry. That is what is
    // actually happening here
    tasks.withType<PublishToMavenRepository>().configureEach {
        if (repository.name == mavenCentral && publication.name == mainPublication) {
            doLast {
                val endpoint =
                    "https://ossrh-staging-api.central.sonatype.com/manual/upload/defaultRepository/$projectNamespace"
                val username = System.getenv("PRODUCTION_MAVEN_CENTRAL_USERNAME")
                val password = System.getenv("PRODUCTION_MAVEN_CENTRAL_PASSWORD")

                if (username.isNullOrBlank() || password.isNullOrBlank()) {
                    throw GradleException(
                        "Missing OSSRH credentials for manual upload request after publication."
                    )
                }

                val credentials = "$username:$password"
                val basicAuthValue = Base64.getEncoder().encodeToString(credentials.toByteArray(StandardCharsets.UTF_8))

                val request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Authorization", "Bearer $basicAuthValue")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build()

                val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
                if (response.statusCode() !in 200..299) {
                    throw GradleException(
                        "Manual OSSRH upload trigger failed with status ${response.statusCode()}: ${response.body()}"
                    )
                }
            }
        }
    }
}
