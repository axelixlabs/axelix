plugins {
    id("shared")
    id("org.springframework.boot") version Dependencies.springBootVersion
    id("com.nucleonforge.axelix-internal")
}

val testcontainersVersion = "1.21.3"
val springDocSwaggerVersion = "2.0.4"
val heapDumpToolVersion = "1.3.3"

dependencies {
    // Self
    implementation(project(":common:domain"))
    implementation(project(":common:api"))
    implementation(project(":common_auth"))
    implementation(project(":common:utils"))

    // Impl
    implementation("org.springframework:spring-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.cloud:spring-cloud-kubernetes-fabric8-discovery")
    implementation("org.slf4j:slf4j-api")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${springDocSwaggerVersion}")
    implementation("com.paypal:heap-dump-tool:${heapDumpToolVersion}")

    implementation("com.github.docker-java:docker-java:3.6.0")
    implementation("com.github.docker-java:docker-java-transport-httpclient5:3.6.0")
    implementation("org.apache.httpcomponents.client5:httpclient5:5.5")
    implementation("org.apache.httpcomponents.core5:httpcore5:5.3.6")

    // Runtime
    runtimeOnly("ch.qos.logback:logback-classic")


    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

// We do not want to generate a regular JAR produced by the "jar" task, Spring Boot plugin will generate what we need
tasks.jar {
    enabled = false
}

tasks.bootJar {
    archiveFileName = "master.jar"
}

publishing {
    publications {
        named<MavenPublication>("nexus") {
            artifact(tasks.bootJar.get())
        }
    }
    publications {
        named<MavenPublication>("gpr") {
            artifact(tasks.bootJar.get())
        }
    }
}

axelix {
    properties.put("version", rootProject.version.toString())
}