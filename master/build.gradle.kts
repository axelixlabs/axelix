plugins {
    id("java")
}

val testcontainersVersion = "1.21.3"
val jsonwebtokenVersion = "0.12.6"
val jsonUnitAssertJVersion = "4.1.1"

dependencies {
    // BOMs
    // TODO:
    //  we do not want to specify the version here, and in Axile Master we want to use the same
    //  version as we use in Axile SBS, because we need to have a compatible API, since Master and SBS
    //  interact with each other.
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))
    testImplementation(platform("com.squareup.okhttp3:okhttp-bom:5.0.0"))

    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // TODO:
    //  is it possible to extract this into a common buildSrc plugin or something?
    //  I do not want to duplicate this dependency, version for sure.
    implementation("org.jspecify:jspecify:1.0.0")
    implementation("org.slf4j:slf4j-api")
    implementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    implementation(project(":common:domain"))
    implementation(project(":common:api"))
    implementation(project(":common_auth"))

    // runtime
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")
    runtimeOnly("ch.qos.logback:logback-classic")

    // Test
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("net.javacrumbs.json-unit:json-unit-assertj:$jsonUnitAssertJVersion")
    testImplementation("com.squareup.okhttp3:mockwebserver")
    testImplementation("com.squareup.okhttp3:okhttp")
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}