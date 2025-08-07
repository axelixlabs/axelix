val testcontainersVersion = "1.21.3"
val jsonwebtokenVersion = "0.12.6"
val axileCommonVersion = "1.0.0-SNAPSHOT"

dependencies {
    // Axile
    implementation("com.nucleon-forge.axile.common:auth:$axileCommonVersion")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-jdbc")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.postgresql:postgresql")
    testImplementation("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    testRuntimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")

    //
    compileOnly("org.springframework.boot:spring-boot-starter-jdbc")
    compileOnly("org.springframework.boot:spring-boot-starter-web")

    api("io.jsonwebtoken:jjwt-api:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jsonwebtokenVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jsonwebtokenVersion")

}
