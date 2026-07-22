plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

// Deliberately no repositories {} block: simulates a closed network with no access to the
// profiler's repository, so it genuinely cannot be resolved.

tasks.register("printDeclaredDeps") {
    // Forces the configuration's build dependencies to be computed before this task runs, which is
    // what actually triggers the plugin's lazy dependency contribution (and, here, its probe).
    dependsOn(configurations["testRuntimeClasspath"])
    doLast {
        configurations["testRuntimeClasspath"].allDependencies.forEach { d ->
            println("DEP>> " + d.group + ":" + d.name + ":" + d.version)
        }
    }
}
