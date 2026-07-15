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
import org.gradle.api.tasks.TaskProvider;

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

    /**
     * The name of the target Gradle compilation task that will depend on the metadata generator.
     */
    private static final String OWNER_TASK_NAME = "compileJava";

    private static final String IMPORTS_FILE_NAME =
        "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";

    @Override
    public void apply(Project project) {
        project.getPlugins().withId("java", javaPlugin -> {
            JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
            SourceSet mainSourceSet = javaExtension.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);

            File resourcesSrcDir = mainSourceSet.getResources().getSrcDirs().iterator().next();
            File importsFile = new File(resourcesSrcDir, IMPORTS_FILE_NAME);

            registerTask(project, mainSourceSet, importsFile);
        });
    }

    private void registerTask(Project project, SourceSet mainSourceSet, File importsFile) {
        TaskProvider<GenerateImportsByAnnotationTask> generateTask = project.getTasks().register(
            "generateAutoConfigImports",
            GenerateImportsByAnnotationTask.class,
            task -> {
                task.getSourceDirectories().from(mainSourceSet.getJava().getSrcDirs());
                task.getOutputFile().set(importsFile);
            }
        );

        project.getTasks().named(OWNER_TASK_NAME)
            .configure(compileJavaTask -> compileJavaTask.dependsOn(generateTask));
    }
}
