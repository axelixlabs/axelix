plugins {
    id("sbs")
    id("com.nucleonforge.axelix-internal")
}

dependencies {
    // Self
    api(project(":common_auth"))
    api(project(":common:api"))
    api(project(":sbs:auth"))
//    api(project(":sbs:postgres"))
    api(project(":sbs:spring"))

    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
    compileOnly("com.zaxxer:HikariCP:6.3.0") // TODO: why? why we need hikari connection pool?
}

axelix {
    properties.put("version", rootProject.version.toString())
}