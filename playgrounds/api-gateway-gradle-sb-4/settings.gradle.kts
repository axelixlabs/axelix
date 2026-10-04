rootProject.name = "api-gateway-gradle-sb-4"

pluginManagement {
    repositories {
        // Axelix artifacts (starter + build plugin) are resolved from the local Maven repo,
        // where `./gradlew publishToMavenLocal` on the monorepo publishes the current -SNAPSHOT.
        mavenLocal()
        gradlePluginPortal()
        mavenCentral()
    }
}
