plugins {
    java
    id("org.springframework.boot") version "4.0.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.6.0"
    // Axelix build plugin (lockstep version). Generates the metadata Axelix Master needs to manage
    // this service. Resolved from mavenLocal (see settings.gradle.kts).
    id("com.axelixlabs.axelix") version "1.4.0-SNAPSHOT"
}

group = "com.axelixlabs.playground"
version = "1.2.0-SNAPSHOT"

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
    implementation("com.axelixlabs:axelix-spring-boot-4-starter:1.4.0-SNAPSHOT")
    implementation("org.springframework.boot:spring-boot-starter-quartz")

    // Stateless gateway: no database, JDBC driver or data starter.
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    // Spring Boot 4 split RestClient auto-configuration into its own module; the gateway forwards
    // outbound calls with it and exercises Axelix's outbound-call monitoring.
    implementation("org.springframework.boot:spring-boot-restclient")
    // Deliberately a maintenance-mode dependency (demo): Apache HttpClient 4.x only receives security
    // fixes now that HttpClient 5 is the active line, so Axelix reports it as MAINTENANCE.
    implementation("org.apache.httpcomponents:httpclient:4.5.14")

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
