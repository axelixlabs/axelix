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

    /** Minimum Thymeleaf version the profiler's HTML report renders on, as {major, minor, patch}. */
    private static final int[] MIN_THYMELEAF_VERSION = {3, 1, 3};

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
     * Whether a declared Thymeleaf version is below {@code 3.1.3}. Only the leading dot-separated
     * numeric components are compared; a trailing qualifier such as {@code .RELEASE} is ignored. A
     * {@code null} or non-numeric version is treated as not below the minimum, leaving an
     * unrecognised version untouched.
     */
    private static boolean isBelowMinimumThymeleaf(final String version) {
        if (version == null) {
            return false;
        }
        int[] components = leadingNumericComponents(version);
        for (int i = 0; i < MIN_THYMELEAF_VERSION.length; i++) {
            int part = i < components.length ? components[i] : 0;
            if (part != MIN_THYMELEAF_VERSION[i]) {
                return part < MIN_THYMELEAF_VERSION[i];
            }
        }
        return false;
    }

    private static int[] leadingNumericComponents(final String version) {
        String[] segments = version.split("\\.");
        int count = 0;
        while (count < segments.length && isNumeric(segments[count])) {
            count++;
        }
        int[] components = new int[count];
        for (int i = 0; i < count; i++) {
            components[i] = Integer.parseInt(segments[i]);
        }
        return components;
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
