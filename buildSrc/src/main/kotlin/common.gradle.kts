plugins {
    id("shared")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

val springBootTestPlatformVersion = "2.7.18"

dependencies {
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
}