plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

group = "com.example"
version = "1.2.3"

repositories { mavenCentral() }

// Build info must be collected unconditionally, even with the profiler feature disabled.
configure<com.axelixlabs.gradle.plugin.AxelixExtension> {
    setCopyProfilerReport(false)
}

// Stands in for Spring Boot's bootJar task, same as bootjar-ordering.gradle.kts.
tasks.register<Jar>("bootJar")
