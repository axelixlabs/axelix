plugins {
    id("sbs")
    id("com.axelixlabs.axelix-internal")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

val springBootTestPlatformVersion = "2.7.18"

dependencies {
    // Self
    api(project(":common:auth"))
    api(project(":common:api"))
    api(project(":common:domain"))
    api(project(":common:utils"))


    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
}