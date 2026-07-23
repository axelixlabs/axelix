plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

group = "com.example"

repositories { mavenCentral() }

tasks.register("printDeclaredDeps") {
    doLast {
        configurations["testRuntimeClasspath"].allDependencies.forEach { d ->
            println("DEP>> " + d.group + ":" + d.name + ":" + d.version)
        }
    }
}
