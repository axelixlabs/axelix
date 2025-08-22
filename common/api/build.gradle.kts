plugins {
    id("common")
}

dependencies {
    api(project(":common:domain"))

    // TODO: it is not correct to specify version up here directly
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.2")
}