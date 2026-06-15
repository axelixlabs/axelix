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

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.bundling.Jar;

/**
 * Copies the Spring Test Profiler HTML report onto the application classpath after tests pass, so
 * the {@code build} task produces a jar containing the report.
 */
final class TestProfilerReportCopy {

    static final String COPY_REPORT_TASK_NAME = "copyAxelixTestProfilerReport";

    private TestProfilerReportCopy() {}

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
    static void configure(final Project project) {
        final File buildDir = BuildDirCompat.buildDir(project);
        final File reportDir = new File(buildDir, "spring-test-profiler");
        final File destinationDir = new File(buildDir, "resources/main/spring-test-profiler");

        Task copyTask = project.getTasks().create(COPY_REPORT_TASK_NAME);
        copyTask.setGroup("axelix");
        copyTask.setDescription("Copies the Spring Test Profiler report onto the application classpath.");
        copyTask.dependsOn("test");
        // Copying from a nonexistent source dir is a silent no-op, so projects whose tests
        // produce no report still build fine.
        copyTask.doLast(task -> {
            project.delete(destinationDir);

            project.copy(spec -> {
                spec.from(reportDir);
                spec.into(destinationDir);
            });
        });

        project.getTasks().getByName("build").dependsOn(copyTask);
        project.getTasks().withType(Jar.class).all(jar -> jar.mustRunAfter(copyTask));
    }
}
