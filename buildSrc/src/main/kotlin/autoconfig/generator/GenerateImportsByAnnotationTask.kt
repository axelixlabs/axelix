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

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.*
import java.io.IOException

/**
 * A custom Gradle task that performs a lightweight static text scan of Java classes
 * to find classes annotated with `@AutoConfiguration`.
 *
 * This task parses classes directly as raw text. It extracts the full package structure
 * and the class name, then generates or updates the Spring Boot configuration imports file
 * inside the source resources directory.
 *
 * @author Vyacheslav Yanin
 * @see AxelixAutoConfigPlugin
 * @see AutoConfigFileWriter
 * @see AutoConfigClassScanner
 */
@CacheableTask
abstract class GenerateImportsByAnnotationTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val classDirectories: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    private val scanner = AutoConfigClassScanner()
    private val writer = AutoConfigFileWriter()

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val autoConfigClasses = scanner.scanForAutoConfigurations(classDirectories.files)
        writer.write(outputFile.get().asFile, autoConfigClasses)
    }
}