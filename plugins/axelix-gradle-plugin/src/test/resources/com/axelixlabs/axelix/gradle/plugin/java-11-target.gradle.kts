plugins {
    id("com.axelixlabs.axelix")
}
apply(plugin = "java")

repositories { mavenCentral() }

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}
