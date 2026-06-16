plugins {
    id("java")
}

// groupId is inherited from the root `allprojects { group = "com.axelixlabs" }` block.
// artifactId is derived from this project's name: `axelix-maven-plugin`.

val mavenApiVersion = "3.9.9"
val mavenAnnotationsVersion = "3.15.1"

dependencies {
    // Provided by the Maven runtime; must not be bundled into the plugin jar.
    compileOnly("org.apache.maven:maven-plugin-api:$mavenApiVersion")
    compileOnly("org.apache.maven.plugin-tools:maven-plugin-annotations:$mavenAnnotationsVersion")
}
