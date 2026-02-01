rootProject.name = "axelix"

include(
    ":master",
    ":sbs",
    ":sbs:axelix-spring-boot-3:auto-configuration",
    ":sbs:axelix-spring-boot-3:spring",
    ":common",
    ":common:api",
    ":common:auth",
    ":common:domain",
    ":common:utils",
)