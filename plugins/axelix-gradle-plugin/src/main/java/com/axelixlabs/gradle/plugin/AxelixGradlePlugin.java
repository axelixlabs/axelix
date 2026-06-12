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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collections;
import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;

/**
 * Adds the Spring Test Profiler to the test runtime classpath of the project and generates the
 * {@code META-INF/spring.factories} registration for it.
 *
 * <p>Compatible with Gradle 4.0 through 9.x. Only APIs present in BOTH Gradle 4.0 and 9.x may be
 * used here: no {@code tasks.register} (added in 4.9), no lazy {@code Provider}/{@code Property}
 * wiring, no sourceSets/conventions access ({@code JavaPluginConvention} was removed in Gradle 9,
 * {@code JavaPluginExtension.getSourceSets()} was only added in 7.1).
 *
 * <p>The plugin is a no-op until the {@code java} plugin is applied, because the
 * {@code testRuntimeOnly} configuration only exists once it is.
 */
public class AxelixGradlePlugin implements Plugin<Project> {

    static final String GENERATE_TASK_NAME = "generateAxelixSpringFactories";

    static final String PROFILER_DEPENDENCY =
            "digital.pragmatech.testing:spring-test-profiler:0.1.2";

    static final String REQUIRED_THYMELEAF_VERSION = "3.1.3.RELEASE";

    static final String SPRING_FACTORIES_CONTENT =
            "org.springframework.test.context.TestExecutionListener=\\\n"
                    + "digital.pragmatech.testing.SpringTestProfilerListener\n"
                    + "org.springframework.context.ApplicationContextInitializer=\\\n"
                    + "digital.pragmatech.testing.diagnostic.ContextDiagnosticApplicationInitializer\n";

    @Override
    public void apply(final Project project) {
        project.getPluginManager().withPlugin("java", appliedPlugin -> configure(project));
    }

    private void configure(Project project) {
        project.getDependencies().add("testRuntimeOnly", PROFILER_DEPENDENCY);

        final File generatedDir = new File(BuildDirCompat.buildDir(project), "generated/axelix");

        Task generateTask = project.getTasks().create(GENERATE_TASK_NAME);
        generateTask.setGroup("axelix");
        generateTask.setDescription(
                "Generates META-INF/spring.factories registering the Spring Test Profiler.");
        // getInputs().properties(Map) keeps the same signature on Gradle 4.0 and 9.x, while
        // getInputs().property(String, Object) changed its return type in 4.3 and would throw
        // NoSuchMethodError on 4.0 daemons.
        generateTask
                .getInputs()
                .properties(Collections.singletonMap("content", SPRING_FACTORIES_CONTENT));
        generateTask.getOutputs().dir(generatedDir);
        generateTask.doLast(task -> writeSpringFactories(generatedDir));

        // Puts the generated directory on the test runtime classpath without touching
        // sourceSets; builtBy ensures the generator runs before any consumer of the
        // configuration.
        project.getDependencies()
                .add("testRuntimeOnly", project.files(generatedDir).builtBy(generateTask));

        project.afterEvaluate(AxelixGradlePlugin::counteractThymeleafDowngrade);
    }

    /**
     * The Spring Boot 2 BOM manages Thymeleaf at 3.0.x, and when it is applied through the
     * {@code io.spring.dependency-management} plugin it overrides (Maven semantics) the 3.1.x
     * version that spring-test-profiler needs to render its HTML report — the report generation
     * then dies silently in its JVM shutdown hook. Bump Thymeleaf back, but only on the test
     * runtime classpath and only when something pinned it below 3.1: Spring Boot 3/4 BOMs already
     * manage 3.1+, and without a forcing BOM Gradle's own conflict resolution picks 3.1.x anyway,
     * so this rule is a no-op everywhere except Spring Boot 2.
     *
     * <p>Registered in {@code afterEvaluate} so the rule runs after (and thus overrides) the
     * dependency-management plugin's own resolution rule.
     */
    private static void counteractThymeleafDowngrade(Project project) {
        Configuration testRuntimeClasspath =
                project.getConfigurations().findByName("testRuntimeClasspath");
        if (testRuntimeClasspath == null) {
            return;
        }
        testRuntimeClasspath
                .getResolutionStrategy()
                .eachDependency(
                        details -> {
                            if ("org.thymeleaf".equals(details.getTarget().getGroup())
                                    && "thymeleaf".equals(details.getTarget().getName())
                                    && isOlderThanThymeleaf31(details.getTarget().getVersion())) {
                                details.useVersion(REQUIRED_THYMELEAF_VERSION);
                            }
                        });
    }

    private static boolean isOlderThanThymeleaf31(String version) {
        return version != null && (version.startsWith("2.") || version.startsWith("3.0."));
    }

    private static void writeSpringFactories(File generatedDir) {
        File target = new File(new File(generatedDir, "META-INF"), "spring.factories");
        File parent = target.getParentFile();
        if (!parent.isDirectory() && !parent.mkdirs()) {
            throw new GradleException("Cannot create directory " + parent);
        }
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(target), "UTF-8")) {
            writer.write(SPRING_FACTORIES_CONTENT);
        } catch (IOException e) {
            throw new GradleException("Failed to write " + target, e);
        }
    }
}
