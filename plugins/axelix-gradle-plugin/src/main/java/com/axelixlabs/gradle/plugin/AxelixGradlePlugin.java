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

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.utils.SemanticVersion;

/**
 * Axelix Gradle plugin entry point.
 *
 * <p>Compatible with Gradle 5.0 through 9.x. Only APIs present in BOTH Gradle 5.0 and 9.x may be
 * used here: no lazy {@code Provider}/{@code Property}
 * wiring, no sourceSets/conventions access ({@code JavaPluginConvention} was removed in Gradle 9,
 * {@code JavaPluginExtension.getSourceSets()} was only added in 7.1).
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
public class AxelixGradlePlugin implements Plugin<Project> {

    private static final String TEST_IMPLEMENTATION = "testImplementation";
    private static final String TEST_RUNTIME_ONLY = "testRuntimeOnly";
    private static final String TEST_RUNTIME_CLASSPATH = "testRuntimeClasspath";

    public static final String PROFILER_GROUP = "digital.pragmatech.testing";
    public static final String PROFILER_NAME = "spring-test-profiler";
    public static final String PROFILER_VERSION = "0.1.2";
    public static final String PROFILER_DEPENDENCY = PROFILER_GROUP + ":" + PROFILER_NAME + ":" + PROFILER_VERSION;

    public static final String THYMELEAF_GROUP = "org.thymeleaf";
    public static final String THYMELEAF_NAME = "thymeleaf";
    public static final String THYMELEAF_VERSION = "3.1.5.RELEASE";
    public static final String THYMELEAF_DEPENDENCY = THYMELEAF_GROUP + ":" + THYMELEAF_NAME + ":" + THYMELEAF_VERSION;
    /** Minimum Thymeleaf version the profiler's HTML report renders on. */
    private static final SemanticVersion MIN_THYMELEAF_VERSION = SemanticVersion.parse("3.1.3");

    @Override
    public void apply(final Project project) {
        // Detection is deferred to afterEvaluate: the build script's dependencies {} block runs after
        // the java plugin is applied, so inspecting the configurations any earlier would always see them
        // empty and wrongly contribute dependencies the user already declared.
        project.getPluginManager().withPlugin("java", appliedPlugin -> project.afterEvaluate(this::configure));
    }

    private void configure(Project project) {
        ModuleComponentIdentifier springBootTestProfiler = findSpringBootTestProfilerDependency(project);

        if (springBootTestProfiler == null) {
            // Spring Boot Test profiler needs thymeleaf to render html templates
            ModuleComponentIdentifier thymeleaf = findThymeleaf(project);

            if (thymeleaf == null) {
                addBothThymeleafAndSpringBootTestProfiler(project);
            } else if (SemanticVersion.parse(thymeleaf.getVersion()).isAtLeast(MIN_THYMELEAF_VERSION)) {
                addJustSpringBootTestProfiler(project);
            } else {
                logUnableToProfileSpringBootTests(project);
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

    private static void addBothThymeleafAndSpringBootTestProfiler(Project project) {
        project.getLogger()
                .info(
                        "Adding thymeleaf of version {} and Spring Boot Test Profiler of version {} to your tests classpath",
                        THYMELEAF_VERSION,
                        PROFILER_VERSION);
        project.getDependencies().add(TEST_IMPLEMENTATION, THYMELEAF_DEPENDENCY);
        configureSpringBootTestProfiler(project);
    }

    private static void configureSpringBootTestProfiler(Project project) {
        project.getDependencies().add(TEST_RUNTIME_ONLY, PROFILER_DEPENDENCY);
        SpringFactoriesGenerator.configure(project);
        SpringTestProfilerReportCopy.configure(project);
    }

    private static void logUnableToProfileSpringBootTests(Project project) {
        project.getLogger()
                .warn(
                        "Unable to profile Spring Boot Tests. Your application uses thymeleaf of version {} (either directly, or indirectly), "
                                + "which is incompatible with Spring Boot Test Profiler. To avoid JAR hell, we're leaving you classpath "
                                + "untouched. In order to take advantage of Spring Boot Test Profiler, please, increase thymeleaf version to at least {}",
                        MIN_THYMELEAF_VERSION,
                        MIN_THYMELEAF_VERSION);
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
