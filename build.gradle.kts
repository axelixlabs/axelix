plugins {
    id("java")
    id("com.diffplug.spotless") version "7.1.0"
}

group = "com.nucleonforge.axile"
version = "1.0.O-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.jspecify:jspecify:1.0.0")

//    // lombok
//    compileOnly("org.projectlombok:lombok")
//    testCompileOnly("org.projectlombok:lombok")
//    annotationProcessor("org.projectlombok:lombok")
//    testAnnotationProcessor("org.projectlombok:lombok")

    // Test
    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:3.0.13"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:2022.0.4"))

    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.test {
    useJUnitPlatform()
}

spotless {
    java {
        palantirJavaFormat("2.69.0")
        target("src/**/*.java")
        importOrder(
            "java",
            "javax",
            "jakarta",
            "",
            "org.springframework",
            "com.nucleonforge",
            "\\#"
        )
        removeUnusedImports()
        removeWildcardImports()
        trimTrailingWhitespace()
    }
}