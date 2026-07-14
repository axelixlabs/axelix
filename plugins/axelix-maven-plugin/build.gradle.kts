plugins {
    id("org.gradlex.maven-plugin-development") version "1.0.3"
    id("maven-publish")
    id("com.gradleup.shadow") version "9.5.1"
}

repositories {
    mavenCentral()
}

tasks.compileJava {
    options.release = 11
}

dependencies {
    implementation(project(":common:utils"))

    compileOnly("org.apache.maven:maven-plugin-api:3.9.16")
    compileOnly("org.apache.maven:maven-core:3.9.16")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:3.15.2")

    testImplementation(platform("org.junit:junit-bom:5.14.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.maven.shared:maven-verifier:1.8.0")
    testImplementation("org.assertj:assertj-core:3.27.6")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

mavenPlugin {
    groupId = "com.axelixlabs"
    artifactId = "axelix-maven-plugin"
}

tasks.publishToMavenLocal {
    dependsOn(tasks.named("generateMavenPluginDescriptor"))
}

// Publication to local .m2 repository needed for the maven verifier to run e2e tests
tasks.test {
    dependsOn(tasks.named("publishToMavenLocal"))
    useJUnitPlatform()
}

tasks.shadowJar {
    dependsOn(tasks.named("generateMavenPluginDescriptor"))
    from("build/mavenPlugin/descriptor/META-INF/maven") {
        into("META-INF/maven")
    }

    archiveClassifier = ""
}

tasks.jar {
    enabled = false
}

publishing {
    publications {
        create<MavenPublication>("mavenPlugin") {
            from(components["shadow"])
        }
    }
}