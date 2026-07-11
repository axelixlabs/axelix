import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
}

// The plugin targets Java 11 at minimum so it can run inside legacy Gradle (5.x) daemons.

// The functional tests run under both the build JDK (modern Gradle) and a Java 11 toolchain
// (legacy Gradle via the legacyGradleTest task), so the test classes must be Java 11 bytecode too.
tasks.withType<JavaCompile> {
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
    api(project(":common:utils"))

    testImplementation(platform("org.junit:junit-bom:5.14.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.assertj:assertj-core:3.27.6")
    testImplementation(gradleTestKit())
}

// No single JVM can launch the whole supported Gradle range: Gradle 5-7 require Java <= 11 while
// Gradle 9 requires Java >= 17. The matrix is therefore split across two test tasks bucketed by the
// JDK each Gradle version can run on; the Gradle versions themselves are the behavioural axis.
tasks.test {
    useJUnitPlatform()
    systemProperty("axelix.test.gradle.versions", "8.10.2,9.5.1")
}

val legacyGradleTest by tasks.registering(Test::class) {
    description = "Runs the functional tests against legacy Gradle versions on a Java 11 toolchain."
    group = "verification"
    useJUnitPlatform()

    val testSourceSet = sourceSets.test.get()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(11)
    }
    systemProperty("axelix.test.gradle.versions", "5.0,6.9,7.6.4")
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(tasks.test)
}