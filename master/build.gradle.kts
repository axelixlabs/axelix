plugins {
    id("java")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("org.slf4j:slf4j-api")
    runtimeOnly("ch.qos.logback:logback-classic")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}