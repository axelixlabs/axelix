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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.util.GradleVersion;

/**
 * Contributes the Spring Test Profiler and Thymeleaf to the project's test classpath when they are
 * missing, leaving a project's own declarations untouched.
 */
final class TestDependencyContributor {

    static final String PROFILER_DEPENDENCY = "digital.pragmatech.testing:spring-test-profiler:0.1.2";

    static final String PROFILER_GROUP = "digital.pragmatech.testing";

    static final String PROFILER_NAME = "spring-test-profiler";

    static final String THYMELEAF_DEPENDENCY = "org.thymeleaf:thymeleaf:3.1.3.RELEASE";

    static final String THYMELEAF_GROUP = "org.thymeleaf";

    static final String THYMELEAF_NAME = "thymeleaf";

    /** Minimum Thymeleaf version the profiler's HTML report renders on. */
    private static final GradleVersion MIN_THYMELEAF_VERSION = GradleVersion.version("3.1.3");

    private TestDependencyContributor() {}

    /**
     * Adds the Spring Test Profiler and Thymeleaf to the test classpath. The profiler is added only
     * when it is not already declared. Thymeleaf is added when it is absent and bumped when a
     * version older than {@code 3.1.3} is declared, because the profiler's HTML report fails to
     * render on older Thymeleaf; a project already on {@code 3.1.3} or newer is left untouched.
     */
    static void contributeMissingTestDependencies(final Project project) {
        if (!isOnTestClasspath(project, PROFILER_GROUP, PROFILER_NAME)) {
            project.getDependencies().add("testRuntimeOnly", PROFILER_DEPENDENCY);
        }
        if (shouldContributeThymeleaf(project)) {
            project.getDependencies().add("testImplementation", THYMELEAF_DEPENDENCY);
        }
    }

    /**
     * Whether the plugin should put Thymeleaf {@code 3.1.3} on the test classpath: it does so when
     * Thymeleaf is not declared at all, or is declared at a version below {@code 3.1.3}. When an
     * older version is pinned directly, the contributed dependency wins by Gradle's newest-version
     * conflict resolution; a project already on {@code 3.1.3} or newer is left untouched.
     */
    private static boolean shouldContributeThymeleaf(final Project project) {
        Dependency declared = findDeclaredOnTestClasspath(project, THYMELEAF_GROUP, THYMELEAF_NAME);
        return declared == null || isBelowMinimumThymeleaf(declared.getVersion());
    }

    private static boolean isOnTestClasspath(final Project project, final String group, final String name) {
        return findDeclaredOnTestClasspath(project, group, name) != null;
    }

    /**
     * Returns the dependency with the given group and name declared on the test runtime classpath,
     * including dependencies inherited from extended configurations (e.g. an {@code implementation}
     * dependency that propagates to {@code testRuntimeClasspath}), or {@code null} if none is
     * declared. Only declared dependencies are inspected; the configuration is not resolved.
     */
    private static Dependency findDeclaredOnTestClasspath(
            final Project project, final String group, final String name) {
        Configuration testRuntimeClasspath = project.getConfigurations().findByName("testRuntimeClasspath");
        if (testRuntimeClasspath == null) {
            return null;
        }
        for (Dependency dependency : testRuntimeClasspath.getAllDependencies()) {
            if (group.equals(dependency.getGroup()) && name.equals(dependency.getName())) {
                return dependency;
            }
        }
        return null;
    }

    /**
     * Whether a declared Thymeleaf version is below {@code 3.1.3}, using {@link GradleVersion} for
     * the comparison. Only the leading dot-separated numeric components are compared; a trailing
     * qualifier such as {@code .RELEASE} is stripped first, since {@link GradleVersion} does not
     * accept it. A {@code null} or non-numeric version is treated as not below the minimum, leaving
     * an unrecognised version untouched.
     */
    private static boolean isBelowMinimumThymeleaf(final String version) {
        if (version == null) {
            return false;
        }
        String numericVersion = leadingNumericVersion(version);
        if (numericVersion == null) {
            return false;
        }
        return GradleVersion.version(numericVersion).getBaseVersion().compareTo(MIN_THYMELEAF_VERSION) < 0;
    }

    /**
     * Extracts the leading dot-separated numeric components of a version (e.g. {@code "3.0.15"} from
     * {@code "3.0.15.RELEASE"}) as a string {@link GradleVersion} can parse, dropping any trailing
     * qualifier. Returns {@code null} when there is no leading numeric component. A single numeric
     * component is padded to {@code major.minor} form, which {@link GradleVersion} requires.
     */
    private static String leadingNumericVersion(final String version) {
        String[] segments = version.split("\\.");
        int count = 0;
        while (count < segments.length && isNumeric(segments[count])) {
            count++;
        }
        if (count == 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder(segments[0]);
        for (int i = 1; i < count; i++) {
            builder.append('.').append(segments[i]);
        }
        if (count == 1) {
            builder.append(".0");
        }
        return builder.toString();
    }

    private static boolean isNumeric(final String segment) {
        if (segment.isEmpty()) {
            return false;
        }
        for (int i = 0; i < segment.length(); i++) {
            if (!Character.isDigit(segment.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
