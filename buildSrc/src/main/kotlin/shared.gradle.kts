plugins {
    id("java-library")
}

val springBootVersion = "3.0.13"
val springCloudDependenciesVersion = "2022.0.4"
val jspecifyVersion = "1.0.0"

dependencies {
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    implementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudDependenciesVersion"))

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testImplementation(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudDependenciesVersion"))
    implementation("org.jspecify:jspecify:$jspecifyVersion")
}