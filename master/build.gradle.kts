import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.axelix

plugins {
    id("shared")
    id("org.springframework.boot") version "4.1.0-RC1"
    id("com.axelixlabs.axelix-internal")
    id("com.axelixlabs.axelix-nodejs")
}

val springBootVersion = "4.1.0-RC1"
val springCloudVersion = "2025.1.1"
val springAiVersion = "2.0.0-M8"

// Not Managed by Spring BOM
val springDocSwaggerVersion = "3.0.3"
val sqliteVersion = "3.53.1.0"
val nimbusJoseJwt ="10.9.1"
val jmesPathVersion = "0.6.0"
val instancioVersion = "5.5.1"

// Explicitly specified versions for security reasons (i.e. using some specific patch versions)
val postgresqlVersion = "42.7.11"
val nettyVersion = "4.2.15.Final"
val tomcatVersion = "11.0.22"
val vertxVersion = "4.5.28"

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

    // Security Patches
    implementation(platform("io.netty:netty-bom:${nettyVersion}"))
    implementation(platform("io.vertx:vertx-stack-depchain:${vertxVersion}"))

    constraints {
        implementation("org.postgresql:postgresql:$postgresqlVersion")
        implementation("org.apache.tomcat.embed:tomcat-embed-core:$tomcatVersion")
        implementation("org.apache.tomcat.embed:tomcat-embed-el:$tomcatVersion")
        implementation("org.apache.tomcat.embed:tomcat-embed-websocket:$tomcatVersion")
    }

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
    implementation("com.github.ben-manes.caffeine:caffeine")
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

    testImplementation("org.testcontainers:testcontainers-postgresql")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.1.1")
    testImplementation("org.instancio:instancio-core:${instancioVersion}")

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

axelix {
    properties.put("version", version.toString())
}

nodejs {
    steps = listOf(
        "ci",
        "run lint",
        "run lint-css",
        "run test",
        "run build",
    )

    sourceDir = project.layout.projectDirectory.dir("front-end").asFile.path
}
