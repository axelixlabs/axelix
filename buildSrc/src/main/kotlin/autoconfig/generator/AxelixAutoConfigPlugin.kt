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
package autoconfig.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.register
import java.io.File

/**
 * Gradle plugin for programmatically generating Spring Boot auto-configuration metadata.
 *
 * This plugin automates the creation and synchronization of the Spring Boot configuration file:
 * `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`.
 * The plugin scans the Java source files for the `@AutoConfiguration` annotation
 * and populates the imports file automatically.
 *
 * @author Vyacheslav Yanin
 * @see GenerateImportsByAnnotationTask
 */
class AxelixAutoConfigPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.plugins.withId("java") {
            registerTask(project)
        }
    }

    private fun registerTask(project: Project) {
        project.tasks.register<GenerateImportsByAnnotationTask>(TASK_NAME) {

            val mainSourceSet = project.extensions
                .getByType(JavaPluginExtension::class.java)
                .sourceSets
                .getByName(SourceSet.MAIN_SOURCE_SET_NAME)

            sourceDirectories.from(mainSourceSet.java.srcDirs)

            val resourcesDir = mainSourceSet.resources.srcDirs.first()
            outputFile.set(File(resourcesDir, IMPORTS_FILE_PATH))

            description = "Generates Spring Boot auto-configuration imports file in src/main/resources"
            group = "axelix"
        }
    }

    companion object {
        private const val TASK_NAME = "generateAutoConfigImports"
        private const val IMPORTS_FILE_PATH =
            "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports"
    }
}
