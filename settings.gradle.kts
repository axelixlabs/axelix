rootProject.name = "axelix"

// FIXME: Workaround for a bug : https://github.com/gradle/gradle/issues/847
include(":common_auth")
project(":common_auth").projectDir = file("common/auth")

include(
    ":master",
    ":sbs",
    ":sbs:auto-configuration",
    ":sbs:metrics",
    ":sbs:postgres",
    ":sbs:spring",
    ":common",
    ":common:api",
    ":common:domain",
    ":common:utils",
)