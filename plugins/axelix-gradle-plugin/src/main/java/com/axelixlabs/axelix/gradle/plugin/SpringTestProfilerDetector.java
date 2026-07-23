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
package com.axelixlabs.axelix.gradle.plugin;

import org.gradle.api.Project;

import static com.axelixlabs.axelix.gradle.plugin.DependencyUtils.findInTheConfigurationClasspath;

/**
 * Detects whether the Spring Boot Test Profiler is present anywhere in the build (e.g. a shared
 * project that other subprojects depend on) and logs the result; it never adds the dependency
 * itself.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
public final class SpringTestProfilerDetector {

    // TODO: this information is duplicated in both gradle/maven plugins. We can and should extract it.
    public static final String PROFILER_GROUP_ID = "digital.pragmatech.testing";
    public static final String PROFILER_ARTIFACT_ID = "spring-test-profiler";
    public static final String PROFILER_DETECTED_PROPERTY = "spring.test.profiler.detected";

    private static final String TEST_RUNTIME_CLASSPATH = "testRuntimeClasspath";

    private SpringTestProfilerDetector() {}

    /**
     * Resolving configurations is only safe once Gradle holds the project's execution lock -
     * {@code projectsEvaluated} runs too early for that (it throws {@code
     * IllegalResolutionException}) - so this is meant to be called from task action code (e.g.
     * {@code doLast}), not from configuration-time callbacks.
     */
    public static boolean isProfilerPresentAnywhereInBuild(Project project) {
        return project.getRootProject().getAllprojects().stream()
                .anyMatch(candidate -> isProfilerPresentIn(project, candidate));
    }

    /**
     * Resolution failures are treated as "not present" for that single project rather than
     * aborting the whole build - this check runs against every project in the build (not just
     * ones with the java plugin applied), so an unrelated project's broken dependency tree must
     * not take down detection for the rest of the build.
     */
    private static boolean isProfilerPresentIn(Project project, Project candidate) {
        if (candidate.getConfigurations().findByName(TEST_RUNTIME_CLASSPATH) == null) {
            return false;
        }

        try {
            return findInTheConfigurationClasspath(
                            candidate, TEST_RUNTIME_CLASSPATH, PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID)
                    != null;
        } catch (Exception e) {
            project.getLogger()
                    .debug(
                            "Could not resolve dependencies of project '{}' for profiler detection",
                            candidate.getPath(),
                            e);
            return false;
        }
    }
}
