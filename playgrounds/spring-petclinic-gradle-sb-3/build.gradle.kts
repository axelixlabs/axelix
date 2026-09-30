buildscript {
  repositories {
    mavenCentral()
  }
  dependencies {
    classpath("commons-io:commons-io:2.22.0")
    classpath("org.apache.commons:commons-lang3:3.20.0")
  }
}

plugins {
  java
  checkstyle
  id("org.springframework.boot") version "3.5.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.graalvm.buildtools.native") version "0.10.6"
  id("org.cyclonedx.bom") version "3.4.1"
  id("com.diffplug.spotless") version "8.6.0"
  id("io.spring.nohttp") version "0.0.11"
  // TODO: pin to 1.3 on the next-but-one Axelix upgrade (compatibility window playground)
  id("com.axelixlabs.axelix") version "1.2.0-SNAPSHOT"
}

group = "org.springframework.samples"
version = "3.5.0-SNAPSHOT"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}
val javaVersionString =
    JavaVersion
        .toVersion(
            java.toolchain.languageVersion
                .get()
                .asInt(),
        ).toString()

repositories {
  mavenLocal()
  mavenCentral()
}

val springJavaformatCheckstyleVersion by ext("0.0.46")
val webjarsLocatorLiteVersion by ext("1.1.0")
val webjarsFontawesomeVersion by ext("4.7.0")
val webjarsBootstrapVersion by ext("5.3.6")

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

dependencies {
  // TODO: pin to 1.3 on the next-but-one Axelix upgrade (compatibility window playground)
  implementation("com.axelixlabs:axelix-spring-boot-3-starter:1.2.0-SNAPSHOT") {
    isChanging = true
  }
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-validation")
  implementation("javax.cache:cache-api")
  implementation("jakarta.xml.bind:jakarta.xml.bind-api")

  runtimeOnly("org.springframework.boot:spring-boot-starter-actuator")
  runtimeOnly("org.webjars:webjars-locator-lite:$webjarsLocatorLiteVersion")
  runtimeOnly("org.webjars.npm:bootstrap:$webjarsBootstrapVersion")
  runtimeOnly("org.webjars.npm:font-awesome:$webjarsFontawesomeVersion")
  runtimeOnly("com.github.ben-manes.caffeine:caffeine")
  runtimeOnly("com.h2database:h2")
  runtimeOnly("com.mysql:mysql-connector-j")
  runtimeOnly("org.postgresql:postgresql")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
  testImplementation("org.springframework.boot:spring-boot-docker-compose")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:mysql")
}

tasks.named<Test>("test") {
  useJUnitPlatform()
}

checkstyle {
  configDirectory.set(project.file("src/checkstyle"))
  configFile = file("src/checkstyle/nohttp-checkstyle.xml")
}

tasks.named<Checkstyle>("checkstyleNohttp") {
  configDirectory.set(project.file("src/checkstyle"))
  configFile = file("src/checkstyle/nohttp-checkstyle.xml")
  notCompatibleWithConfigurationCache("The NoHTTP Checkstyle task captures the project model.")
}

tasks.wrapper {
  gradleVersion = "8.14.3"
  distributionType = Wrapper.DistributionType.ALL
}

tasks.bootJar {
    archiveFileName = "spring-petclinic-3.5.0-SNAPSHOT.jar"
}

spotless {
  java {
    palantirJavaFormat("2.90.0")
    target("src/**/*.java")
    forbidWildcardImports()
    trimTrailingWhitespace()
    removeUnusedImports("cleanthat-javaparser-unnecessaryimport")
  }

  kotlin {
    target("src/**/*.kt")
    trimTrailingWhitespace()
  }
}
