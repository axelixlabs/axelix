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
import org.gradle.api.Task;

import java.io.File;

/**
 * Copies spring test profiler report to application class path
 *
 * @author Artemiy Degtyarev
 */
final class SpringTestProfilerReportCopy {

    public static final String COPY_PROFILER_REPORT_TASK_NAME = "copyProfilerReport";

    static void configure(final Project project) {
        Task copyTask = project.getTasks().create(COPY_PROFILER_REPORT_TASK_NAME);
        copyTask.setGroup("axelix");
        copyTask.setDescription("Copies spring-test-profiler report to class path");

        File buildDir = BuildDirAccessor.buildDir(project);
        File reportDir = new File(buildDir, "spring-test-profiler");
        File destinationDir = new File(buildDir, "resources/main/profiler-reports");

        copyTask.doLast(task -> {
            project.delete(destinationDir);

            project.copy(spec -> {
                spec.from(reportDir);
                spec.into(destinationDir);
            });
        });
    }
}
