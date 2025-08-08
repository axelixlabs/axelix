plugins {
    id("java")
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jspecify:jspecify:1.0.0")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}