plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
}

val springBootVersion = "4.0.6"
val springCloudVersion = "2025.1.0"

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
    compileOnly("org.springframework.boot:spring-boot-starter-cache")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.github.ben-manes.caffeine:caffeine")
    compileOnly("io.micrometer:micrometer-core")

    // processor
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor:${springBootVersion}")

    // Test
    testImplementation(project(":sbs:starter-domain"))
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-restclient")
    testImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testImplementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.h2database:h2")
    testImplementation("com.github.ben-manes.caffeine:caffeine")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.1.1")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 17
}

axelix {
    properties.put("version", rootProject.version.toString())
}
