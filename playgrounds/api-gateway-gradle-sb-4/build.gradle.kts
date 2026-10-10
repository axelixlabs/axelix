plugins {
    java
    id("org.springframework.boot") version "4.0.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.6.0"
    // Axelix build plugin (lockstep version). Generates the metadata Axelix Master needs to manage
    // this service. Resolved from mavenLocal (see settings.gradle.kts).
    id("com.axelixlabs.axelix") version "1.2.0"
}

group = "com.axelixlabs.playground"
version = "1.2.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    // Axelix Spring Boot 4 starter (lockstep version, resolved from mavenLocal).
    implementation("com.axelixlabs:axelix-spring-boot-4-starter:1.2.0")

    // This is a stateless edge / API-gateway service: web + actuator only.
    //
    // There is DELIBERATELY no spring-boot-starter-data-jpa / -jdbc and no JDBC driver here, so the
    // whole spring-tx module (org.springframework:spring-tx) never reaches the runtime classpath.
    // That is the exact setup that used to crash the Axelix starter on boot (see GH-1708): the
    // TransactionMonitoringBeanPostProcessor references org.springframework.transaction.* and failed
    // to load. This playground verifies the starter now boots cleanly in that configuration.
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Spring Boot 4 split RestClient auto-configuration into its own module; the gateway forwards
    // outbound calls with it (and this exercises Axelix's outbound-call monitoring, which stays
    // active without spring-tx).
    implementation("org.springframework.boot:spring-boot-restclient")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

spotless {
    java {
        palantirJavaFormat("2.90.0")
        target("src/**/*.java")
        forbidWildcardImports()
        trimTrailingWhitespace()
        removeUnusedImports("cleanthat-javaparser-unnecessaryimport")
    }
}

tasks.wrapper {
    gradleVersion = "8.14.3"
    distributionType = Wrapper.DistributionType.ALL
}
