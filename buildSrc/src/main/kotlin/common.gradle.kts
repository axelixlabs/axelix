/*
 * Copyright (C) 2025-2026 Axelix Labs
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
plugins {
    id("shared")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 11
}

val jacksonDatabindVersion = "2.22.4"
val springBootTestPlatformVersion = "2.7.18"

dependencies {
    // compileOnly

    // We intentionally use Jackson 2.13.5 for Spring Boot 2.7.x and 3.0.x compatibility:
    // Spring Boot 2 relies on Jackson 2.13 at minimum, and Spring Boot 3 relies on Jackson 2.19
    // at its base. Because of the current design we cannot move this dependency out of common.
    // This would lead to over complication that we do not need at least know. Because now we support
    // Only Spring Boot 2 and Spring Boot 3, we do not need to worry about Jackson 3 incompatibility.
    compileOnly("com.fasterxml.jackson.core:jackson-databind:${jacksonDatabindVersion}")

    testImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootTestPlatformVersion"))

    // Gradle needs it to launch the Junit tests, and, unfortunately, spring-boot-starter-test in 2.x
    // does NOT include the launcher, however, it includes the Junit engine, so, we need the launcher only
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}