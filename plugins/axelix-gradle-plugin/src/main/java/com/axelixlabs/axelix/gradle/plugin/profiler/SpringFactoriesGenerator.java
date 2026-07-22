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
package com.axelixlabs.axelix.gradle.plugin.profiler;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.Task;

import com.axelixlabs.axelix.gradle.plugin.BuildDirAccessor;

/**
 * Generates the {@code META-INF/spring.factories} registration for the Spring Test Profiler and
 * puts the generated directory on the test runtime classpath.
 *
 * @author Artemiy Degtyarev
 * @author Nikita Kirillov
 */
public final class SpringFactoriesGenerator {

    public static final String GENERATE_TASK_NAME = "generateAxelixSpringFactories";
    public static final String SPRING_FACTORIES_CONTENT =
            "\norg.springframework.test.context.TestExecutionListener=\\\n"
                    + "digital.pragmatech.testing.SpringTestProfilerListener\n"
                    + "org.springframework.context.ApplicationContextInitializer=\\\n"
                    + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    private SpringFactoriesGenerator() {}

    public static void configure(final Project project) {
        final File generatedDir = generatedDir(project);

        Task generateTask = project.getTasks().create(GENERATE_TASK_NAME);
        generateTask.setGroup("build");
        generateTask.setDescription("Generates META-INF/spring.factories registering the Spring Test Profiler.");
        // getInputs().properties(Map) keeps the same signature on Gradle 5.0 and 9.x
        generateTask.getInputs().properties(Collections.singletonMap("content", SPRING_FACTORIES_CONTENT));
        // Tracked as an input (not just read inside doLast) so that a flip in profiler
        // resolvability invalidates the task's up-to-date state - otherwise Gradle would see
        // unchanged inputs/outputs across builds and skip doLast entirely, leaving a stale
        // spring.factories behind (or failing to generate one once it becomes resolvable).
        generateTask
                .getInputs()
                .property("profilerPresent", project.provider(() -> isProfilerOnTestClasspath(project)));
        generateTask.getOutputs().dir(generatedDir);
        generateTask.doLast(task -> {
            if (isProfilerOnTestClasspath(project)) {
                writeSpringFactories(generatedDir);
            } else {
                deleteSpringFactories(generatedDir);
            }
        });
    }

    /**
     * Puts the generated directory on the test runtime classpath without touching sourceSets;
     * builtBy ensures the generator runs before any consumer of the configuration.
     */
    public static void addToTestClasspath(Project project) {
        project.getDependencies()
                .add("testRuntimeOnly", project.files(generatedDir(project)).builtBy(GENERATE_TASK_NAME));
    }

    /**
     * Forces real resolution of the actual {@code testRuntimeClasspath} (not a detached copy of its
     * declared dependencies), so that if nothing else in this build has resolved it yet, this check
     * itself is what triggers the lazy profiler contribution registered via {@code
     * Configuration#withDependencies} - otherwise, running this task in isolation would never see a
     * dependency that only gets added lazily upon resolution.
     */
    private static boolean isProfilerOnTestClasspath(Project project) {
        return project
                .getConfigurations()
                .getByName("testRuntimeClasspath")
                .getResolvedConfiguration()
                .getResolvedArtifacts()
                .stream()
                .anyMatch(artifact -> artifact.getModuleVersion()
                                .getId()
                                .getGroup()
                                .equals(SpringTestProfilerConfigurer.PROFILER_GROUP)
                        && artifact.getModuleVersion()
                                .getId()
                                .getName()
                                .equals(SpringTestProfilerConfigurer.PROFILER_NAME));
    }

    private static File generatedDir(Project project) {
        return new File(BuildDirAccessor.buildDir(project), "generated/axelix");
    }

    private static void writeSpringFactories(File generatedDir) {
        File target = new File(new File(generatedDir, "META-INF"), "spring.factories");
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new GradleException("Cannot create directory " + parent);
        }

        try {
            Files.write(
                    Paths.get(target.getPath()),
                    SPRING_FACTORIES_CONTENT.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new GradleException("Failed to write " + target, e);
        }
    }

    /**
     * Removes a previously generated {@code spring.factories} once the profiler is no longer
     * resolvable, so a stale registration referencing a missing listener class doesn't linger on
     * the test classpath.
     */
    private static void deleteSpringFactories(File generatedDir) {
        File target = new File(new File(generatedDir, "META-INF"), "spring.factories");
        if (target.exists() && !target.delete()) {
            throw new GradleException("Cannot delete " + target);
        }
    }
}
