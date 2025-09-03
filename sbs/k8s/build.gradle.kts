plugins {
    id("sbs")
}

val junitPioneerVersion = "2.3.0"

dependencies {
    // Compile
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    // Test
    testImplementation("org.junit-pioneer:junit-pioneer:$junitPioneerVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    jvmArgs(
        "--add-opens", "java.base/java.util=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED"
    )
}