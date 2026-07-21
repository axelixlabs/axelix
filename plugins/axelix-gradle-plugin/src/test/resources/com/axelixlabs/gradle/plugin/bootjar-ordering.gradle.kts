plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

group = "com.example"

repositories { mavenCentral() }

// Stands in for Spring Boot's bootJar task without pulling in the Spring Boot plugin: the copy
// task wiring only keys off the task name and AbstractArchiveTask type.
tasks.register<Jar>("bootJar")
