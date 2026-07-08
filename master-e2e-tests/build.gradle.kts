import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("shared")
}

val restAssuredVersion = "5.5.7"
val awaitilityVersion = "4.3.0"
val assertjVersion = "3.27.7"
val junitVersion = "6.1.1"
val jsoupVersion = "1.22.2"

dependencies {
    testImplementation("io.rest-assured:rest-assured:${restAssuredVersion}")
    testImplementation("io.rest-assured:json-path:${restAssuredVersion}")
    testImplementation("org.awaitility:awaitility:${awaitilityVersion}")
    testImplementation("org.assertj:assertj-core:${assertjVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testImplementation("org.jsoup:jsoup:${jsoupVersion}")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.test {
    // enable for local testing
    enabled = false
    useJUnitPlatform()

    systemProperty("axelix.e2e.masterBaseUrl", "http://localhost:8080")
    systemProperty("axelix.e2e.superAdmin.username", "admin")
    systemProperty("axelix.e2e.superAdmin.password", "admin")

    systemProperty("axelix.e2e.discoveryModeAutoEnabled", "false")
    systemProperty("axelix.e2e.discoveryModeSelfRegEnabled", "false")

    systemProperty("axelix.e2e.authModeLocalEnabled", "true")
    systemProperty("axelix.e2e.authModeOAuth2Enabled", "false")

    systemProperty("axelix.e2e.mcpEnabled", "true")
}

// Comment out the lines below for local testing
val e2eTest by tasks.registering(Test::class) {
    testClassesDirs = tasks.test.get().testClassesDirs
    classpath = tasks.test.get().classpath

    val isRunEnabled = providers.gradleProperty("axelix.e2e.run").getOrElse("false").toBoolean()
    enabled = isRunEnabled
    useJUnitPlatform()

    if (isRunEnabled) {
        systemProperty("axelix.e2e.masterBaseUrl", providers.gradleProperty("axelix.e2e.masterBaseUrl").get())
        systemProperty("axelix.e2e.superAdmin.username", providers.gradleProperty("axelix.e2e.superAdmin.username").getOrElse("admin"))
        systemProperty("axelix.e2e.superAdmin.password", providers.gradleProperty("axelix.e2e.superAdmin.password").getOrElse("admin"))

        // Discovery mode
        systemProperty("axelix.e2e.discoveryModeAutoEnabled", providers.gradleProperty("axelix.e2e.discoveryModeAutoEnabled").get())
        systemProperty("axelix.e2e.discoveryModeSelfRegEnabled", providers.gradleProperty("axelix.e2e.discoveryModeSelfRegEnabled").get())

        // Auth mode
        systemProperty("axelix.e2e.authModeLocalEnabled", providers.gradleProperty("axelix.e2e.authModeLocalEnabled").get())
        systemProperty("axelix.e2e.authModeOAuth2Enabled", providers.gradleProperty("axelix.e2e.authModeOAuth2Enabled").get())

        // MCP server
        systemProperty("axelix.e2e.mcpEnabled", providers.gradleProperty("axelix.e2e.mcpEnabled").get())
    }
}