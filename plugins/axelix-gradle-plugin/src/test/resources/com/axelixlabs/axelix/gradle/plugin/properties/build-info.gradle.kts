plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

group = "com.example"
version = "1.2.3"

repositories { mavenCentral() }

// Stands in for Spring Boot's bootJar task without pulling in the Spring Boot plugin.
tasks.register<Jar>("bootJar")
