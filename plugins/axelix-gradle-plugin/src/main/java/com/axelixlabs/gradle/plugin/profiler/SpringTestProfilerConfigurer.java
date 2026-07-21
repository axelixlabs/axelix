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
package com.axelixlabs.gradle.plugin.profiler;

import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.gradle.plugin.AxelixExtension;
import com.axelixlabs.gradle.plugin.DependencyUtils;

/**
 * Wires up the Spring Test Profiler: contributes its test dependency (and Thymeleaf, if it is
 * missing from the classpath), generates the {@code spring.factories} registration, and copies
 * its HTML report into the {@code bootJar}.
 *
 * <p>Thymeleaf compatibility is not our concern to police: if it's already on the classpath we
 * use it as-is, and if not, the profiler's own POM pulls in a version it's tested against.
 *
 * <p>When {@link AxelixExtension#isCopyProfilerReport()} is {@code false}, this feature is a
 * complete no-op: no dependency is added, no task is registered, the task graph is left exactly
 * as every other plugin configured it. This lets users bail out entirely if the feature ever
 * conflicts with their own plugins or task graph.
 *
 * @author Artemiy Degtyarev
 * @author Nikita Kirillov
 */
public final class SpringTestProfilerConfigurer {

    private static final String TEST_RUNTIME_ONLY = "testRuntimeOnly";
    private static final String TEST_RUNTIME_CLASSPATH = "testRuntimeClasspath";

    public static final String PROFILER_GROUP = "digital.pragmatech.testing";
    public static final String PROFILER_NAME = "spring-test-profiler";
    public static final String PROFILER_VERSION = "0.1.2";
    public static final String PROFILER_DEPENDENCY = PROFILER_GROUP + ":" + PROFILER_NAME + ":" + PROFILER_VERSION;

    public static final String THYMELEAF_GROUP = "org.thymeleaf";
    public static final String THYMELEAF_NAME = "thymeleaf";

    private SpringTestProfilerConfigurer() {}

    public static void configure(Project project, AxelixExtension extension) {
        if (!extension.isCopyProfilerReport()) {
            project.getLogger()
                    .info("Spring Test Profiler is disabled via the axelix extension; leaving the build untouched");
            return;
        }

        ModuleComponentIdentifier springBootTestProfiler = findSpringBootTestProfilerDependency(project);

        if (springBootTestProfiler == null) {
            // Spring Boot Test profiler needs thymeleaf to render html templates
            if (findThymeleaf(project) == null) {
                addSpringBootTestProfilerRelyingOnTransitiveThymeleaf(project);
            } else {
                addJustSpringBootTestProfiler(project);
            }
        } else {
            project.getLogger()
                    .info(
                            "Everything's good, Spring Boot Test profiler of version {} is already included",
                            springBootTestProfiler.getVersion());
        }
    }

    private static void addJustSpringBootTestProfiler(Project project) {
        project.getLogger()
                .info("Adding just Spring Boot Test Profiler of version {} to your tests classpath", PROFILER_VERSION);
        configureSpringBootTestProfiler(project);
    }

    private static void addSpringBootTestProfilerRelyingOnTransitiveThymeleaf(Project project) {
        project.getLogger()
                .info(
                        "Adding Spring Boot Test Profiler of version {} to your tests classpath; thymeleaf is not "
                                + "present, so it will be pulled in transitively by the profiler",
                        PROFILER_VERSION);
        configureSpringBootTestProfiler(project);
    }

    private static void configureSpringBootTestProfiler(Project project) {
        project.getDependencies().add(TEST_RUNTIME_ONLY, PROFILER_DEPENDENCY);
        SpringFactoriesGenerator.configure(project);
        SpringTestProfilerReportCopy.configure(project);
    }

    private static @Nullable ModuleComponentIdentifier findThymeleaf(Project project) {
        return DependencyUtils.findInTheConfigurationClasspath(
                project, TEST_RUNTIME_CLASSPATH, THYMELEAF_GROUP, THYMELEAF_NAME);
    }

    private static @Nullable ModuleComponentIdentifier findSpringBootTestProfilerDependency(Project project) {
        return DependencyUtils.findInTheConfigurationClasspath(
                project, TEST_RUNTIME_CLASSPATH, PROFILER_GROUP, PROFILER_NAME);
    }
}
