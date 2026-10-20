plugins {
    java
    id("org.springframework.boot") version "4.0.7"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.diffplug.spotless") version "8.6.0"
    id("com.axelixlabs.axelix") version "1.3.0-SNAPSHOT"
}

group = "com.axelixlabs.playground"
version = "1.3.0-SNAPSHOT"

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
    implementation("com.axelixlabs:axelix-spring-boot-4-starter:1.3.0-SNAPSHOT")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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
