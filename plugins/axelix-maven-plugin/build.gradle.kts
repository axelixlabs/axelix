plugins {
    id("org.gradlex.maven-plugin-development") version "1.0.3"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

tasks.compileJava {
    options.release = 8
}

dependencies {
    implementation("org.apache.maven:maven-plugin-api:3.9.16")
    implementation("org.apache.maven:maven-core:3.9.16")

    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.15.2")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.maven.shared:maven-verifier:1.8.0")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

mavenPlugin {
    groupId = "com.axelixlabs"
    artifactId = "axelix-maven-plugin"
}

tasks.publishToMavenLocal {
    dependsOn(tasks.named("generateMavenPluginDescriptor"))
}

tasks.test {
    dependsOn(tasks.named("publishToMavenLocal"))
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenPlugin") {
            from(components["java"])
        }
    }
}