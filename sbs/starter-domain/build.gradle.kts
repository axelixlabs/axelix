import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    id("shared")
    kotlin("jvm") version "2.3.21"
}

val springBootTestPlatformVersion = "2.7.18"

dependencies {
    // Self
    api(project(":common:auth"))
    api(project(":common:api"))
    api(project(":common:domain"))
    api(project(":common:utils"))

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-web")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.1.1")

    // Gradle needs it to launch the Junit tests, and, unfortunately, spring-boot-starter-test in 2.x
    // does NOT include the launcher, however, it includes the Junit engine, so, we need the launcher only
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
    options.compilerArgs.add("-parameters")
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {}

        val concurrencyTestsSuite = "concurrencyTest"

        register<JvmTestSuite>(concurrencyTestsSuite) {

            sources {
                kotlin {
                    setSrcDirs(listOf("src/$concurrencyTestsSuite/kotlin"))
                }
            }

            dependencies {
                implementation("org.jetbrains.lincheck:lincheck:3.6")
                implementation("org.jetbrains.kotlin:kotlin-stdlib")

                // Additional Test Suites do not inherit production dependencies automatically.
                implementation(project(":sbs:starter-domain"))
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks {
    withType(KotlinJvmCompile::class).configureEach {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_11)
    }
}
