import Dependencies.jspecifyVersion
import Dependencies.springBootVersion
import Dependencies.springCloudDependenciesVersion

plugins {
    id("java-library")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:${springBootVersion}"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:${springCloudDependenciesVersion}"))

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudDependenciesVersion"))
    implementation("org.jspecify:jspecify:${jspecifyVersion}")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}