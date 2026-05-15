import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.kotlin.dsl.axelix

plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
    id("com.axelixlabs.axelix-nodejs")
    id("java-test-fixtures")
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
    api(project(":common:domain"))
    api(project(":common:api"))
    api(project(":common:auth"))
    api(project(":common:utils"))

    // Impl
    api(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    api(platform("org.springframework.ai:spring-ai-bom:${springAiVersion}"))

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
    api("org.springframework.boot:spring-boot-starter-data-jdbc")
    api("org.springframework.boot:spring-boot-starter-restclient")
    api("org.springframework.boot:spring-boot-starter-webmvc")
    api("org.springframework.boot:spring-boot-starter-actuator")
    api("org.springframework.boot:spring-boot-starter-liquibase")

    api("org.springframework.cloud:spring-cloud-kubernetes-fabric8-discovery")
    api("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")
    api("org.springframework.security:spring-security-crypto")

    api("org.slf4j:slf4j-api")
    api("com.github.ben-manes.caffeine:caffeine")
    api("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocSwaggerVersion}")
    api("com.nimbusds:nimbus-jose-jwt:${nimbusJoseJwt}")

    // TODO:
    //  This library is archived, and it only supports Jackson 2.x
    //  For now we're gonna get away with it, but Spring Boot 4 already
    //  establishes a baseline for Jackson 3, but it still ca work with Jackson 2.
    //  So, we need to decide how are we going to work with this.
    api("io.burt:jmespath-jackson:${jmesPathVersion}")

    // Runtime
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("org.xerial:sqlite-jdbc:${sqliteVersion}")

    // Test Self
    testImplementation(testFixtures(project))
    testFixturesImplementation(project(":common:domain"))
    testFixturesImplementation(project(":common:api"))
    testFixturesImplementation(project(":common:auth"))
    testFixturesImplementation(project(":common:utils"))

    // Test
    testFixturesApi(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testFixturesApi(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    testFixturesApi("org.springframework.boot:spring-boot-starter-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-jdbc-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-restclient-test")
    testFixturesApi("org.springframework.boot:spring-boot-starter-webmvc-test")
    testFixturesApi("org.springframework.boot:spring-boot-testcontainers")

    testFixturesApi("org.testcontainers:testcontainers-postgresql")
    testFixturesApi("org.testcontainers:testcontainers-mysql")
    testFixturesApi("org.testcontainers:testcontainers-junit-jupiter")
    testFixturesApi("com.squareup.okhttp3:mockwebserver")
    testFixturesApi("com.squareup.okhttp3:okhttp")
    testFixturesApi("digital.pragmatech.testing:spring-test-profiler:0.1.1")
    testFixturesApi("org.instancio:instancio-core:${instancioVersion}")

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

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 25
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
