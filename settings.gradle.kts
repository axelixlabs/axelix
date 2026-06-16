rootProject.name = "axelix"

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
    ":plugins:axelix-maven-plugin",
)