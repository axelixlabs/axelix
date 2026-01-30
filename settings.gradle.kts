rootProject.name = "axelix"

// FIXME: Workaround for a bug : https://github.com/gradle/gradle/issues/847
include(":common_auth")
project(":common_auth").projectDir = file("common/auth")

include(
    ":master",
    ":sbs",
    ":sbs:axelix-spring-boot-3:auto-configuration",
    ":sbs:axelix-spring-boot-3:spring",
    ":common",
    ":common:api",
    ":common:domain",
    ":common:utils",
)