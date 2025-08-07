plugins {
    id("sbs")
}

dependencies {
    implementation(project(":sbs:metrics"))
    api("org.postgresql:postgresql")

    testImplementation("org.junit.jupiter:junit-jupiter-api")
}
