plugins {
    java
    id("com.axelixlabs.axelix")
}

group = "com.example"
version = "1.2.3"

repositories { mavenCentral() }

// slf4j-simple pulls in slf4j-api transitively, giving the SBOM a known root -> direct ->
// transitive chain to assert the resolution graph against.
dependencies {
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

// Stands in for Spring Boot's bootJar task without pulling in the Spring Boot plugin.
tasks.register<Jar>("bootJar")
