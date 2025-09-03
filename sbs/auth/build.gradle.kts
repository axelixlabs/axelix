plugins {
    id("sbs")
}

val jsonwebtokenVersion = "0.12.6"

dependencies {
    // Self
    implementation(project(":common_auth"))

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")

    // Api
    api("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
}
