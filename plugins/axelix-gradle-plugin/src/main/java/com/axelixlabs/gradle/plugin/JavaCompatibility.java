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
package com.axelixlabs.gradle.plugin;

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.util.GradleVersion;
import org.jspecify.annotations.Nullable;

/**
 * Determines the Java release the project's {@code compileJava} task targets, across Gradle 5.0
 * through 9.x.
 *
 * @author Nikita Kirillov
 */
public final class JavaCompatibility {

    private static final GradleVersion RELEASE_OPTION_SUPPORTED = GradleVersion.version("6.6");

    private JavaCompatibility() {}

    /**
     * @return {@code true} if the project's {@code compileJava} task targets at least {@code minimum}.
     *         {@code false} if it targets an older release, or the target cannot be determined
     *         (e.g. no {@code compileJava} task).
     */
    public static boolean compilesToAtLeast(Project project, JavaVersion minimum) {
        JavaVersion effective = effectiveJavaVersion(project);
        return effective != null && effective.compareTo(minimum) >= 0;
    }

    private static @Nullable JavaVersion effectiveJavaVersion(Project project) {
        Task compileJava = project.getTasks().findByName("compileJava");
        if (!(compileJava instanceof JavaCompile)) {
            return null;
        }

        JavaCompile javaCompile = (JavaCompile) compileJava;

        JavaVersion release = releaseVersion(javaCompile);
        if (release != null) {
            return release;
        }

        String targetCompatibility = javaCompile.getTargetCompatibility();
        return targetCompatibility == null ? null : JavaVersion.toVersion(targetCompatibility);
    }

    private static @Nullable JavaVersion releaseVersion(JavaCompile javaCompile) {
        if (GradleVersion.current().getBaseVersion().compareTo(RELEASE_OPTION_SUPPORTED) < 0) {
            return null;
        }
        return ReleaseOption.read(javaCompile);
    }

    /**
     * Separate class so that Gradle versions older than 6.6, which lack {@code
     * CompileOptions.getRelease()}, never resolve it.
     */
    private static final class ReleaseOption {

        private ReleaseOption() {}

        static @Nullable JavaVersion read(JavaCompile javaCompile) {
            Integer release = javaCompile.getOptions().getRelease().getOrNull();
            return release == null ? null : JavaVersion.toVersion(release.toString());
        }
    }
}
