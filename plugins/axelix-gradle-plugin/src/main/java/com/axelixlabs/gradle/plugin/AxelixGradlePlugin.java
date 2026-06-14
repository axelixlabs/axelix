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
import org.gradle.api.tasks.bundling.Jar;

/**
 * Adds the Spring Test Profiler to the test runtime classpath of the project, generates the
 * {@code META-INF/spring.factories} registration for it, and copies the profiler's HTML report
 * onto the application classpath ({@code build/resources/main}) after tests pass, so that the
 * {@code build} task produces a jar containing the report.
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

    static final String COPY_REPORT_TASK_NAME = "copyAxelixTestProfilerReport";

    static final String PROFILER_DEPENDENCY =
            "digital.pragmatech.testing:spring-test-profiler:0.1.2";

    static final String THYMELEAF_DEPENDENCY = "org.thymeleaf:thymeleaf:3.1.3.RELEASE";

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
        project.getDependencies().add("testImplementation", THYMELEAF_DEPENDENCY);

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

        configureReportCopy(project);
    }

    /**
     * Copies the profiler report into {@code build/resources/main} so it ends up on the
     * application runtime classpath. The task depends on {@code test}, so it never runs when
     * tests fail. It deliberately declares no inputs/outputs: the destination is owned by
     * {@code processResources}, and registering it as an output here would create overlapping
     * outputs that degrade caching — the report is regenerated on every test run anyway, so an
     * always-run copy is correct. Jar tasks are ordered after the copy so the report lands in
     * the packaged jar deterministically instead of depending on whether {@code jar} happened
     * to run before or after {@code test}.
     */
    private static void configureReportCopy(final Project project) {
        final File buildDir = BuildDirCompat.buildDir(project);
        final File reportDir = new File(buildDir, "spring-test-profiler");
        final File destinationDir = new File(buildDir, "resources/main/spring-test-profiler");

        Task copyTask = project.getTasks().create(COPY_REPORT_TASK_NAME);
        copyTask.setGroup("axelix");
        copyTask.setDescription(
                "Copies the Spring Test Profiler report onto the application classpath.");
        copyTask.dependsOn("test");
        // Copying from a nonexistent source dir is a silent no-op, so projects whose tests
        // produce no report still build fine.
        copyTask.doLast(
                task -> {
                    project.delete(destinationDir);

                    project.copy(
                        spec -> {
                            spec.from(reportDir);
                            spec.into(destinationDir);
                        });
                });

        project.getTasks().getByName("build").dependsOn(copyTask);
        project.getTasks().withType(Jar.class).all(jar -> jar.mustRunAfter(copyTask));
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
