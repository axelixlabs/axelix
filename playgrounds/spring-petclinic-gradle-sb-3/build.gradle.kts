plugins {
  java
  checkstyle
  id("org.springframework.boot") version "3.5.0"
  id("io.spring.dependency-management") version "1.1.7"
  id("org.graalvm.buildtools.native") version "0.10.6"
  id("org.cyclonedx.bom") version "2.3.1"
  id("io.spring.javaformat") version "0.0.46"
  id("io.spring.nohttp") version "0.0.11"
}

gradle.startParameter.excludedTaskNames += listOf("checkFormatAot", "checkFormatAotTest")

group = "org.springframework.samples"
version = "3.5.0"

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(17))
  }
}

repositories {
  mavenCentral()
}

val checkstyleVersion by ext("10.25.0")
val springJavaformatCheckstyleVersion by ext("0.0.46")
val webjarsLocatorLiteVersion by ext("1.1.0")
val webjarsFontawesomeVersion by ext("4.7.0")
val webjarsBootstrapVersion by ext("5.3.6")

configurations.all {
  resolutionStrategy.cacheChangingModulesFor(0, TimeUnit.SECONDS)
}

dependencies {
  implementation("com.axelixlabs:axelix-spring-boot-3-starter:1.0.0-M2") {
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

  "checkstyle"("io.spring.javaformat:spring-javaformat-checkstyle:$springJavaformatCheckstyleVersion")
  "checkstyle"("com.puppycrawl.tools:checkstyle:$checkstyleVersion")
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
}

tasks.named("formatMain").configure {
  dependsOn("checkstyleMain")
  dependsOn("checkstyleNohttp")
}

tasks.named("formatTest").configure {
  dependsOn("checkstyleTest")
  dependsOn("checkstyleNohttp")
}

tasks.named("checkstyleAot") { enabled = false }
tasks.named("checkstyleAotTest") { enabled = false }
tasks.named("checkFormatAot") { enabled = false }
tasks.named("checkFormatAotTest") { enabled = false }
tasks.named("formatAot") { enabled = false }
tasks.named("formatAotTest") { enabled = false }

tasks.wrapper {
  gradleVersion = "8.14.3"
  distributionType = Wrapper.DistributionType.ALL
}
