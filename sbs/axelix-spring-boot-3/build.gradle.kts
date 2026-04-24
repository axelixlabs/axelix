plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
}

val springBootVersion = "3.0.13"
val springCloudVersion = "2022.0.4"

dependencies {
    // Self
    api(project(":sbs:starter-domain"))

    // Impl
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    implementation("org.slf4j:slf4j-api")
    implementation("com.jayway.jsonpath:json-path") // version comes from spring-boot-dependencies

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.github.ben-manes.caffeine:caffeine")

    // processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.h2database:h2")
    testImplementation("com.github.ben-manes.caffeine:caffeine")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 17
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {}

        val concurrencyTestsSuite = "concurrencyTest"

        register<JvmTestSuite>(concurrencyTestsSuite) {

            sources {
                java {
                    setSrcDirs(listOf("src/$concurrencyTestsSuite/java"))
                }
            }

            useJUnitJupiter()

            dependencies {
                implementation("org.jetbrains.lincheck:lincheck:3.5")

                // This is the dependency for the compiled production. Additional Test Suites do not have them
                // in any of their Gradle configurations.
                implementation(project())
            }

            targets {
                all {
                    testTask.configure {

                        // Soft-dependency on the default Test Suite (like basic junit unit/integration tests)
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}


axelix {
    properties.put("version", rootProject.version.toString())
}
