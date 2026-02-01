plugins {
    id("shared")
}

java {
    toolchain {
        // The classes inside the common modules are expected to be going to
        // be used in the various Java environments, starting from Java 11 (Spring Boot 2)
        languageVersion = JavaLanguageVersion.of(11)
    }
}

val springBootTestPlatformVersion = "3.0.13"

dependencies {
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
}