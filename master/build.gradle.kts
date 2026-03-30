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
val heapDumpToolVersion = "1.3.3"
val sqliteVersion = "3.51.2.0"
val nimbusJoseJwt ="10.8"

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
    implementation("org.springframework.boot:spring-boot-starter-jdbc") // TODO: Do we still need plain JDBC starter? I think no...
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-restclient")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    implementation("org.springframework.cloud:spring-cloud-kubernetes-fabric8-discovery")
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")

    implementation("org.slf4j:slf4j-api")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocSwaggerVersion}")
    implementation("com.paypal:heap-dump-tool:${heapDumpToolVersion}")
    implementation("com.nimbusds:nimbus-jose-jwt:${nimbusJoseJwt}")

    // Runtime
    runtimeOnly("ch.qos.logback:logback-classic")
    runtimeOnly("org.xerial:sqlite-jdbc:${sqliteVersion}")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")

    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
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
    properties.put("version", rootProject.version.toString())
}