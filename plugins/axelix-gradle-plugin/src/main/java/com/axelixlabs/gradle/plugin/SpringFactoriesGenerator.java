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

/**
 * Generates the {@code META-INF/spring.factories} registration for the Spring Test Profiler and
 * puts the generated directory on the test runtime classpath.
 */
final class SpringFactoriesGenerator {

    public static final String GENERATE_TASK_NAME = "generateAxelixSpringFactories";
    public static final String SPRING_FACTORIES_CONTENT = "org.springframework.test.context.TestExecutionListener=\\\n"
            + "digital.pragmatech.testing.SpringTestProfilerListener\n"
            + "org.springframework.context.ApplicationContextInitializer=\\\n"
            + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    private SpringFactoriesGenerator() {}

    static void configure(final Project project) {
        final File generatedDir = new File(BuildDirAccessor.buildDir(project), "generated/axelix");

        Task generateTask = project.getTasks().create(GENERATE_TASK_NAME);
        generateTask.setGroup("axelix");
        generateTask.setDescription("Generates META-INF/spring.factories registering the Spring Test Profiler.");
        // getInputs().properties(Map) keeps the same signature on Gradle 5.0 and 9.x
        generateTask.getInputs().properties(Collections.singletonMap("content", SPRING_FACTORIES_CONTENT));
        generateTask.getOutputs().dir(generatedDir);
        generateTask.doLast(task -> writeSpringFactories(generatedDir));

        // Puts the generated directory on the test runtime classpath without touching
        // sourceSets; builtBy ensures the generator runs before any consumer of the
        // configuration.
        project.getDependencies()
                .add("testRuntimeOnly", project.files(generatedDir).builtBy(generateTask));
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
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            throw new GradleException("Failed to write " + target, e);
        }
    }
}
