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

import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;
import org.gradle.api.tasks.testing.Test;

import com.axelixlabs.axelix.gradle.plugin.BuildDirAccessor;
import com.axelixlabs.axelix.gradle.plugin.JavaCompatibility;

/**
 * Copies spring test profiler report to application class path
 *
 * @author Artemiy Degtyarev
 * @author Nikita Kirillov
 */
public final class SpringTestProfilerReportCopy {

    public static final String COPY_PROFILER_REPORT_TASK_NAME = "copyProfilerReport";

    // JavaVersion.VERSION_17 is a field reference that would fail to link under Gradle versions
    // predating Java 17 (e.g. 5.0), whose bundled JavaVersion enum has no such constant.
    // toVersion(...) is a plain method call, safe on any Gradle version.
    private static final JavaVersion MIN_JAVA_VERSION = JavaVersion.toVersion("17");

    private SpringTestProfilerReportCopy() {}

    public static void configure(final Project project) {
        File baseGeneratedDir = new File(BuildDirAccessor.buildDir(project), "generated/axelix-report");

        File targetResourceDir = new File(baseGeneratedDir, "META-INF/axelix");

        TaskProvider<Copy> copyReportTask = project.getTasks()
                .register(COPY_PROFILER_REPORT_TASK_NAME, Copy.class, task -> {
                    task.setGroup("verification");
                    task.setDescription("Extracts latest.html for JAR packaging.");

                    File sourceDir = new File(BuildDirAccessor.buildDir(project), "spring-test-profiler");

                    task.from(sourceDir);
                    task.into(targetResourceDir);
                    task.include("latest.html");
                    task.dependsOn(project.getTasks().withType(Test.class));

                    task.onlyIf(t -> JavaCompatibility.compilesToAtLeast(project, MIN_JAVA_VERSION));
                });

        project.getTasks().configureEach(task -> {
            String taskName = task.getName();
            if ("bootJar".equals(taskName)) {
                task.dependsOn(copyReportTask);

                if (task instanceof AbstractArchiveTask) {
                    ((AbstractArchiveTask) task).from(baseGeneratedDir);
                }
            }
        });
    }
}
