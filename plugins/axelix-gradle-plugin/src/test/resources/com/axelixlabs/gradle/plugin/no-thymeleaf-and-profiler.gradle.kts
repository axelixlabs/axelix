plugins {
    id("com.axelixlabs.axelix")
}
// Applied after our plugin on purpose: exercises the withPlugin reaction.
apply(plugin = "java")

repositories { mavenCentral() }

tasks.register("printTestRuntimeClasspath") {
    dependsOn(configurations["testRuntimeClasspath"])
    doLast {
        configurations["testRuntimeClasspath"].allDependencies.forEach { d ->
            println("DEP>> " + d.group + ":" + d.name + ":" + d.version)
        }
        // Resolved (including transitive) modules, so tests can tell a transitively pulled-in
        // dependency apart from one the plugin declared explicitly.
        configurations["testRuntimeClasspath"].resolvedConfiguration.resolvedArtifacts.forEach { a ->
            println("RESOLVED>> " + a.moduleVersion.id.group + ":" + a.moduleVersion.id.name + ":" + a.moduleVersion.id.version)
        }
    }
}
