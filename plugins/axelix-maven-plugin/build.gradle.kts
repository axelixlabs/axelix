plugins {
    id("org.gradlex.maven-plugin-development") version "1.0.3"
    id("maven-publish")
}

repositories {
    mavenCentral()
}

tasks.compileJava {
    options.release = 11
}

// pinned to the same version the gradle plugin uses, for consistency across the two.
val jgitVersion = "6.10.1.202505221210-r"
val mavenPluginVersion = "3.9.16"
val mavenPluginAnnotationsVersion = "3.15.2"
val junitBomVersion = "5.14.4"
val mavenVerifierVersion = "1.8.0"
val assertjVersion = "3.27.6"

dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:${jgitVersion}")

    compileOnly("org.apache.maven:maven-plugin-api:${mavenPluginVersion}")
    compileOnly("org.apache.maven:maven-core:${mavenPluginVersion}")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:${mavenPluginAnnotationsVersion}")

    testImplementation(platform("org.junit:junit-bom:${junitBomVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.apache.maven.shared:maven-verifier:${mavenVerifierVersion}")
    testImplementation("org.apache.maven:maven-core:${mavenPluginVersion}")
    testImplementation("org.assertj:assertj-core:${assertjVersion}")

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
