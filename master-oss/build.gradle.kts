plugins {
    id("master-runtime")
    id("com.axelixlabs.axelix-nodejs")
    id("org.springframework.boot") version "4.2.0"
    id("com.axelixlabs.axelix-internal")
}

dependencies {
    // Self
    implementation(project(":master"))
}

configurations.all {
    exclude(group = "org.apache.logging.log4j", module = "log4j-api")
    exclude(group = "org.apache.logging.log4j", module = "log4j-to-slf4j")
}

springBoot {
    mainClass = "com.axelixlabs.axelix.master.app.MasterApplication"
}

tasks.bootJar {
    archiveFileName = "master.jar"
}

axelix {
    properties.put("version", version.toString())
}

nodejs {
    sourceDir = layout.projectDirectory.dir("front-end").asFile.path
    steps = listOf("run build")
}
