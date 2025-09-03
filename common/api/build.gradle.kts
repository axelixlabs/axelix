plugins {
    id("common")
}

dependencies {
    // Self
    api(project(":common:domain"))

    // Impl
    implementation("com.fasterxml.jackson.core:jackson-databind")
}