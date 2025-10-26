import Dependencies.jsonwebtokenVersion

plugins {
    id("sbs")
}

dependencies {
    // Self
    implementation(project(":common_auth"))

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
}
