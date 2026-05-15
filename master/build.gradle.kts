import org.apache.tools.ant.filters.ReplaceTokens

plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
    id("java-test-fixtures")
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
    api(project(":common:domain"))
    api(project(":common:api"))
    api(project(":common:auth"))
    api(project(":common:utils"))

    // Impl
    api(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    api(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudVersion}"))
    api(platform("org.springframework.ai:spring-ai-bom:${springAiVersion}"))

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

    testFixturesApi("org.testcontainers:postgresql:$testcontainersVersion")
    testFixturesApi("org.testcontainers:mysql:$testcontainersVersion")
    testFixturesApi("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testFixturesApi("org.postgresql:postgresql")
    testFixturesApi("com.mysql:mysql-connector-j")
    testFixturesApi("com.squareup.okhttp3:mockwebserver")
    testFixturesApi("com.squareup.okhttp3:okhttp")
    testFixturesApi("digital.pragmatech.testing:spring-test-profiler:0.1.0")

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
