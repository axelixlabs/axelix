plugins {
    id("sbs")
}

val jsonwebtokenVersion = "0.12.6"

dependencies {
    // Self
    implementation(project(":common_auth"))
    implementation(project(":sbs:auth"))
    implementation(project(":sbs:postgres"))
    implementation(project(":sbs:spring"))

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.zaxxer:HikariCP:6.3.0")
    compileOnly("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
}
