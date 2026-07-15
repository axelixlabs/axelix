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
package com.axelixlabs.plugin.autoconfig.generator;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

import java.io.File;

/**
 * Gradle plugin for programmatically generating Spring Boot auto-configuration metadata.
 * <p>
 * This plugin automates the creation and synchronization of the Spring Boot configuration file:
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 * The plugin scans the Java source files for the {@code @AutoConfiguration} annotation
 * and populates the imports file automatically.
 * </p>
 *
 * @author Vyacheslav Yanin
 * @see GenerateImportsByAnnotationTask
 */
public class AxelixAutoConfigPlugin implements Plugin<Project> {

    private static final String TASK_NAME = "generateAutoConfigImports";
    private static final String IMPORTS_FILE_PATH =
        "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", javaPlugin -> registerTask(project));
    }

    private void registerTask(Project project) {
        project.getTasks().register(TASK_NAME, GenerateImportsByAnnotationTask.class, task -> {
            SourceSet mainSourceSet = project.getExtensions()
                .getByType(JavaPluginExtension.class)
                .getSourceSets()
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            task.getSourceDirectories().from(mainSourceSet.getJava().getSrcDirs());

            File resourcesDir = mainSourceSet.getResources().getSrcDirs().iterator().next();
            task.getOutputFile().set(new File(resourcesDir, IMPORTS_FILE_PATH));

            task.setDescription("Generates Spring Boot auto-configuration imports file in src/main/resources");
            task.setGroup("axelix");
        });
    }
}
