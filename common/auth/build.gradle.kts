plugins {
    id("common")
}

val jsonwebtokenVersion = "0.12.6"

dependencies {
    implementation("jakarta.servlet:jakarta.servlet-api")
    implementation("org.slf4j:slf4j-api")

    // Api
    api("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    api("org.springframework:spring-web")
    api("org.springframework:spring-context")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")

    // Test
    testImplementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
}
