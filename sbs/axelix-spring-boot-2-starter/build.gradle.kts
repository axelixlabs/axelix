plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
}

val springBootVersion = "2.7.18"
val springCloudVersion = "2021.0.9"

dependencies {
    // Self
    compileOnly(project(":sbs:starter-domain"))

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
    testImplementation(project(":sbs:starter-domain"))
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testImplementation("org.springframework.cloud:spring-cloud-starter-loadbalancer")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.h2database:h2")
    testImplementation("com.github.ben-manes.caffeine:caffeine")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")

    // Test Runtime

    // Gradle needs it to launch the Junit tests, and, unfortunately, spring-boot-starter-test in 2.x
    // does NOT include the launcher, however, it includes the Junit engine, so, we need the launcher only
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

configurations.testRuntimeClasspath {
    resolutionStrategy.force("org.slf4j:slf4j-api:1.7.36")
    resolutionStrategy.force("ch.qos.logback:logback-classic:1.2.12")
    resolutionStrategy.force("ch.qos.logback:logback-core:1.2.12")
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = 11
}

axelix {
    properties.put("version", rootProject.version.toString())
}
