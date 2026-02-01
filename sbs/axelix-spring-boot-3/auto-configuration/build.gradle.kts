import Dependencies.springBootVersion
import Dependencies.springCloudDependenciesVersion

plugins {
    id("sbs")
    id("com.axelixlabs.axelix-internal")
}

dependencies {
    // Impl
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudDependenciesVersion}"))

    // Self
    api(project(":common:auth"))
    api(project(":common:api"))
    api(project(":sbs:axelix-spring-boot-3:spring"))

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.zaxxer:HikariCP:6.3.0") // TODO: why? why we need hikari connection pool?
}

axelix {
    properties.put("version", rootProject.version.toString())
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}