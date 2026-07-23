plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

group = "com.example"

// Deliberately no repositories {} block, but a dependency IS declared: detection tries to resolve
// testRuntimeClasspath's already-declared dependency graph, and this genuinely fails (simulating
// e.g. a closed network with no access to the profiler's repository) - the build must not fail
// because of it; that project is just treated as "profiler not present".
dependencies {
    "testRuntimeOnly"("digital.pragmatech.testing:spring-test-profiler:0.1.2")
}
