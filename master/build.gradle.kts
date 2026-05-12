import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("shared")
    id("org.springframework.boot") version "4.0.1"
    id("com.axelixlabs.axelix-internal")
}

val springBootVersion = "4.0.5"
val springCloudVersion = "2025.1.1"
val springAiVersion = "2.0.0-M2"
val testcontainersVersion = "1.21.3"
val springDocSwaggerVersion = "3.0.1"
val sqliteVersion = "3.51.2.0"
val nimbusJoseJwt ="10.8"
val jmesPathVersion = "0.6.0"

dependencies {
    // Self
    implementation(project(":common:domain"))
    implementation(project(":common:api"))
    implementation(project(":common:auth"))
    implementation(project(":common:utils"))

    // Impl
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    implementation(platform("org.springframework.ai:spring-ai-bom:${springAiVersion}"))

    // Boot Starters
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    implementation("org.springframework.cloud:spring-cloud-kubernetes-fabric8-discovery")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
    implementation("org.springframework.security:spring-security-crypto")

    implementation("org.slf4j:slf4j-api")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocSwaggerVersion}")
    implementation("com.nimbusds:nimbus-jose-jwt:${nimbusJoseJwt}")

    // TODO:
    //  This library is archived, and it only supports Jackson 2.x
    //  For now we're gonna get away with it, but Spring Boot 4 already
    //  establishes a baseline for Jackson 3, but it still ca work with Jackson 2.
    //  So, we need to decide how are we going to work with this.
    implementation("io.burt:jmespath-jackson:${jmesPathVersion}")

    // Runtime
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.xerial:sqlite-jdbc:${sqliteVersion}")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:mysql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("com.mysql:mysql-connector-j")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.1.0")

    // annotation processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:$springBootVersion")
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// We do not want to generate a regular JAR produced by the "jar" task, Spring Boot plugin will generate what we need
tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveFileName = "master.jar"
}

tasks.processResources {

    val projectVersion = version.toString()

    filesMatching("application.yaml") {
        // ReplaceTokens is used for ant-like style placeholders to not clash with Spring Boot ${} syntax

        filter<ReplaceTokens>(
            "tokens" to mapOf("project.version" to projectVersion)
        )
    }

    exclude("application-local.yaml")
}

publishing {
    publications {
        named<MavenPublication>("nexus") {
            artifact(tasks.bootJar.get())
        }
    }
    publications {
        named<MavenPublication>("gpr") {
            artifact(tasks.bootJar.get())
        }
    }
}

axelix {
    properties.put("version", version.toString())
}
