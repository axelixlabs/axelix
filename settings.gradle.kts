rootProject.name = "axelix"

val enterpriseDir = file("axelix-enterprise")
if (enterpriseDir.exists() && file("axelix-enterprise/master-enterprise/build.gradle.kts").exists()) {
    include(":master-enterprise")
    project(":master-enterprise").projectDir = file("axelix-enterprise/master-enterprise")
}

include(
    ":master",
    ":master-e2e-tests",
    ":sbs:axelix-spring-boot-2-starter",
    ":sbs:axelix-spring-boot-3-starter",
    ":sbs:axelix-spring-boot-4-starter",
    ":sbs:starter-domain",
    ":common",
    ":plugins:axelix-gradle-plugin",
    ":plugins:axelix-maven-plugin",
)