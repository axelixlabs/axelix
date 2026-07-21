plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

// No Spring Boot plugin, no bootJar task: only the standard 'jar' task exists.
group = "com.example"
version = "1.2.3"

repositories { mavenCentral() }
