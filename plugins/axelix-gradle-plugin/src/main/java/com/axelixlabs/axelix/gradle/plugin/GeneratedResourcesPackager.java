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

import java.io.File;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.bundling.AbstractArchiveTask;

/**
 * Wires a directory of generated resources into the project's archive: {@code jar} for plain Java
 * projects, {@code bootJar} for Spring Boot ones.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
public final class GeneratedResourcesPackager {

    private GeneratedResourcesPackager() {}

    public static void packageIntoArchives(Project project, Task generateTask, File generatedDir) {
        project.getTasks().configureEach(task -> {
            String taskName = task.getName();
            if ("jar".equals(taskName) || "bootJar".equals(taskName)) {
                task.dependsOn(generateTask);

                if (task instanceof AbstractArchiveTask) {
                    // Since processResources also copies the same files into build/resources/main
                    // (see below), the standard 'jar' task (and a real Spring Boot 'bootJar', which
                    // includes the main sourceSet output by default) would see each file twice -
                    // once via that main output, once via this explicit 'from'. Both copies are
                    // byte-for-byte identical (same generated file), so excluding the duplicate is
                    // safe.
                    ((AbstractArchiveTask) task)
                            .from(generatedDir, spec -> spec.setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE));
                }
            } else if ("processResources".equals(taskName)) {
                // Without this, the files only end up inside the packaged jar/bootJar (via the
                // wiring above) - `test`, `bootRun` and IDE runs use build/resources/main directly
                // and would never see them.
                task.dependsOn(generateTask);

                if (task instanceof Copy) {
                    ((Copy) task).from(generatedDir);
                }
            }
        });
    }
}
