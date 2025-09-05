import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("shared")
    id("org.graalvm.buildtools.native") version "0.11.0"
    id("org.springframework.boot") version Dependencies.springBootVersion
}

val testcontainersVersion = "1.21.3"
val jsonwebtokenVersion = "0.12.6"
val jsonUnitAssertJVersion = "3.5.0"
val instancioVersion = "5.5.1"

dependencies {
    // Self
    implementation(project(":common:domain"))
    implementation(project(":common:api"))
    implementation(project(":common_auth"))

    // Impl
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.slf4j:slf4j-api")
    implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")

    // Runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
    runtimeOnly("ch.qos.logback:logback-classic")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$jsonUnitAssertJVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
    testImplementation("org.instancio:instancio-core:${instancioVersion}")
}

tasks.named("nativeCompile") {
    dependsOn("processAot")
}

// Disable PMD checks for AOT and native-image related tasks
tasks.withType<Pmd>().configureEach {
    if (name.contains("aot", ignoreCase = true) ||
        name.contains("native", ignoreCase = true)) {
        println("Disabling PMD for task: $name")
        enabled = false
    }
}

// Disable ErrorProne (NullAway) for AOT and native-image related tasks
tasks.withType<JavaCompile>().configureEach {
    if (name.contains("aot", ignoreCase = true) ||
        name.contains("native", ignoreCase = true)) {

        println("Disabling ErrorProne for task: $name")
        options.errorprone.isEnabled = false
    }
}

graalvmNative {
    binaries {
        named("main") {
            mainClass.set("com.nucleonforge.axile.master.ApplicationEntrypoint")
            imageName.set("master")                                         // Output binary name
            sharedLibrary.set(false)                                        // Build executable (not shared library)
            verbose.set(true)                                               // Print detailed build logs
            fallback.set(false)                                             // Disable fallback mode
            buildArgs.add("--enable-http")
            buildArgs.add("--enable-https")
        }
    }
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

// We do not want to generate a regular JAR produced by the "jar" task, Spring Boot plugin will generate what we need
tasks.jar {
    enabled = project.findProperty("enableJar") == "true"
}

// Enable JAR only for native-image builds
gradle.taskGraph.whenReady {
    if (allTasks.any { it.name == "nativeCompile" }) {
        tasks.jar.get().enabled = true
    }
}

tasks.bootJar {
    archiveFileName = "master.jar"
}