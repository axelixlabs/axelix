plugins {
    id("common")
}

dependencies {
    // Compile
    compileOnly(project(":common:api"))
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.junit.platform:junit-platform-launcher")
}