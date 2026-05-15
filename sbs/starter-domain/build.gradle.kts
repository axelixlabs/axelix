plugins {
    id("shared")
    id("com.axelixlabs.axelix-internal")
}

val springBootTestPlatformVersion = "2.7.18"

dependencies {
    // Self
    api(project(":common:auth"))
    api(project(":common:api"))
    api(project(":common:domain"))
    api(project(":common:utils"))

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework:spring-web")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("digital.pragmatech.testing:spring-test-profiler:0.1.0")

    // Gradle needs it to launch the Junit tests, and, unfortunately, spring-boot-starter-test in 2.x
    // does NOT include the launcher, however, it includes the Junit engine, so, we need the launcher only
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
    options.compilerArgs.add("-parameters")
}
