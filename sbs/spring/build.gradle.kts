plugins {
    id("sbs")
}

dependencies {

    testImplementation(project(":sbs:auto-configuration"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testImplementation("org.springframework.cloud:spring-cloud-starter-openfeign")
    testImplementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("com.h2database:h2")

    // Self
    implementation(project(":common:domain"))

    //
    compileOnly("org.springframework.boot:spring-boot-starter-actuator")
    compileOnly("org.springframework.boot:spring-boot-starter-web")
    compileOnly("org.springframework.cloud:spring-cloud-starter-openfeign")
    compileOnly("org.springframework.kafka:spring-kafka")
}

tasks {
    val nonStandardVmOptionsTest by registering(Test::class) {
        useJUnitPlatform {
            include("**/DefaultNonStandardVMOptionsDiscovererTest.class")
        }
        jvmArgs = listOf(
            "-Xms256m",
            "-Xmx512m",
            "-XX:+UseG1GC"
        )
    }

    test {
        useJUnitPlatform {
            exclude("**/DefaultNonStandardVMOptionsDiscovererTest.class")
        }
        dependsOn(nonStandardVmOptionsTest)
    }
}