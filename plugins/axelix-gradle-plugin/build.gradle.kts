plugins {
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

// The plugin must run inside Gradle 4.0 daemons, which require JDK 8.
tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

gradlePlugin {
    plugins {
        create("axelix") {
            id = "com.axelixlabs.axelix"
            implementationClass = "com.axelixlabs.gradle.plugin.AxelixGradlePlugin"
        }
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.14.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation(gradleTestKit())
}

tasks.test {
    useJUnitPlatform()
}
