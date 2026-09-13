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
            implementationClass = "com.axelixlabs.axelix.gradle.plugin.AxelixGradlePlugin"
        }
    }
}

// pinned to the 6.x line since 7.x requires Java 17+, above this plugin's Java 11 floor.
val jgitVersion = "6.10.1.202505221210-r"
val junitBomVersion = "5.14.4"
val assertjVersion = "3.27.7"
dependencies {
    implementation("org.eclipse.jgit:org.eclipse.jgit:${jgitVersion}")
    implementation("org.cyclonedx:cyclonedx-core-java:13.2.0")

    testImplementation(platform("org.junit:junit-bom:${junitBomVersion}"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:${assertjVersion}")
    testImplementation(gradleTestKit())
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

val currentJvmGradleVersions = listOf("9.5.1")
val gradle810Versions = listOf("8.10.2")
// The plugin itself supports Gradle 5.0+, but the functional tests can only run against 7.6+.
// cyclonedx-core-java (used to generate the dependency SBOM) drags in a multi-release Jackson jar,
// and GradleRunner.withPluginClasspath() injects the plugin classpath by re-jarring every entry -
// something TestKit could not do for a multi-release jar until Gradle 7.6. Older Gradle versions
// therefore fail at TestKit startup with "Failed to create Jar file ... jackson-core-*.jar", so
// they are excluded from the matrix. This is a test-harness limitation only: real consumers on
// Gradle 5.x/6.x resolve the published plugin normally and are unaffected.
val legacyGradleVersions = listOf("7.6.4")

// No single JVM can launch the whole supported Gradle range: Gradle 5-7 require Java <= 11, Gradle
// 8.10.2 bundles a Groovy that can't compile build scripts on JDKs newer than it was released for
// (e.g. fails with "Unsupported class file major version" on JDK 25), and Gradle 9 requires Java
// >= 17. The matrix is therefore split across test tasks bucketed by the JDK each Gradle version
// can run on, each pinned via a toolchain so results don't depend on whatever JDK happens to be on
// PATH/JAVA_HOME wherever the outer build is invoked from (console, CI, an IDE's own Gradle JVM,
// etc.) - the Gradle versions themselves are the behavioural axis.
tasks.test {
    useJUnitPlatform()
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    systemProperty("axelix.test.gradle.versions", currentJvmGradleVersions.joinToString(separator = ","))
}

val gradle810Test by tasks.registering(Test::class) {
    description = "Runs the functional tests against Gradle 8.10.2 on a JDK it actually supports."
    group = "verification"
    useJUnitPlatform()

    val testSourceSet = sourceSets.test.get()
    testClassesDirs = testSourceSet.output.classesDirs
    classpath = testSourceSet.runtimeClasspath

    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    systemProperty("axelix.test.gradle.versions", gradle810Versions.joinToString(separator = ","))
    shouldRunAfter(tasks.test)
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
    systemProperty("axelix.test.gradle.versions", legacyGradleVersions.joinToString(separator = ","))
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(tasks.test, gradle810Test)
}