plugins {
    id("sbs")
}

dependencies {
    // Self
    implementation(project(":sbs:metrics"))

    // Api
    api("org.postgresql:postgresql")

    // Test
    testImplementation("org.junit.jupiter:junit-jupiter-api")
}
