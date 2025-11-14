rootProject.name = "axile"

// FIXME: Workaround for a bug : https://github.com/gradle/gradle/issues/847
include(":common_auth")
project(":common_auth").projectDir = file("common/auth")

include(
    ":master",
    ":sbs",
    ":sbs:auth",
    ":sbs:k8s",
    ":sbs:auto-configuration",
    ":sbs:hotspot",
    ":sbs:metrics",
    ":sbs:postgres",
    ":sbs:spring",
    ":common",
    ":common:api",
    ":common:domain",
    ":common:utils",
)