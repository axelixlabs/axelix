plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

repositories { mavenCentral() }

configure<com.axelixlabs.axelix.gradle.plugin.AxelixExtension> {
    setCopyProfilerReport(false)
}

tasks.register("printDeclaredDeps") {
    doLast {
        configurations["testRuntimeClasspath"].allDependencies.forEach { d ->
            println("DEP>> " + d.group + ":" + d.name + ":" + d.version)
        }
    }
}
