plugins {
    id("sbs")
}

val jsonwebtokenVersion = "0.12.6"

dependencies {
    // Axile
    implementation(project(":common_auth"))

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")

    //
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")

    api("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
}
