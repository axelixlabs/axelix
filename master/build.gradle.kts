plugins {
    id("shared")
    id("org.springframework.boot") version Dependencies.springBootVersion
}

val testcontainersVersion = "1.21.3"
val jsonwebtokenVersion = "0.12.6"
val jsonUnitAssertJVersion = "3.5.0"
val instancioVersion = "5.5.1"
val springDocSwaggerVersion = "2.0.4"

dependencies {
    // Self
    implementation(project(":common:domain"))
    implementation(project(":common:api"))
    implementation(project(":common_auth"))

    // Impl
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-kubernetes-discovery")
    implementation("org.slf4j:slf4j-api")
    implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
    runtimeOnly("ch.qos.logback:logback-classic")

    // Swagger(OpenAPI)
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springDocSwaggerVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$jsonUnitAssertJVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("org.instancio:instancio-core:${instancioVersion}")
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

// We do not want to generate a regular JAR produced by the "jar" task, Spring Boot plugin will generate what we need
tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveFileName = "master.jar"
}