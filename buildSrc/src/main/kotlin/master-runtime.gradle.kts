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
import org.gradle.jvm.toolchain.JavaLanguageVersion

/**
 * Convention plugin for the Axelix Master runtime modules (the `:master` library and the
 * per-distribution assembly modules built on top of it). It pins the single Java version those
 * modules compile and run against, so the toolchain is declared in exactly one place.
 */

plugins {
    id("shared")
}

val masterJavaVersion = 27

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(masterJavaVersion)
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-parameters")
    options.release = masterJavaVersion
}
