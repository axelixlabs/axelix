rootProject.name = "axelix"

val enterpriseDir = file("axelix-enterprise")
if (enterpriseDir.exists() && file("axelix-enterprise/master-enterprise/build.gradle.kts").exists()) {
    include(":master-enterprise")
    project(":master-enterprise").projectDir = file("axelix-enterprise/master-enterprise")
}

include(
    ":master",
    ":sbs:axelix-spring-boot-2-starter",
    ":sbs:axelix-spring-boot-3-starter",
    ":sbs:axelix-spring-boot-4-starter",
    ":sbs:starter-domain",
    ":common:api",
    ":common:auth",
    ":common:domain",
    ":common:utils",
    ":plugins:axelix-gradle-plugin",
)