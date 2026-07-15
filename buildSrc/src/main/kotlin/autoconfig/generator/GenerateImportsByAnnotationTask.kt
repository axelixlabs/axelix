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
import java.nio.file.Files

/**
 * A custom Gradle task that performs a lightweight static text scan of Java source files
 * to find classes annotated with `@AutoConfiguration`.
 *
 * This task parses source files directly as raw text. It extracts the full package structure
 * and the class name, then generates or updates the Spring Boot configuration imports file
 * inside the source resources directory.
 *
 * @author Vyacheslav Yanin
 * @see AxelixAutoConfigPlugin
 * @see AutoConfigFileWriter
 * @see JavaFileParser
 */
@CacheableTask
abstract class GenerateImportsByAnnotationTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDirectories: ConfigurableFileCollection

    @get:OutputFile
    abstract val outputFile: RegularFileProperty

    private val parser = JavaFileParser()
    private val writer = AutoConfigFileWriter()

    @TaskAction
    @Throws(IOException::class)
    fun generate() {
        val autoConfigClasses = scanForAutoConfigurations()
        writer.write(outputFile.get().asFile, autoConfigClasses)
    }

    private fun scanForAutoConfigurations(): List<String> {
        val classes = LinkedHashSet<String>()
        for (sourceDir in sourceDirectories.files) {
            if (!sourceDir.isDirectory) continue

            try {
                Files.walk(sourceDir.toPath())
                    .filter(Files::isRegularFile)
                    .filter { it.toString().endsWith(".java") }
                    .forEach { path -> parseJavaFile(path, classes) }
            } catch (e: IOException) {
                logger.warn("Failed to scan directory: $sourceDir", e)
            }
        }
        return classes.toList()
    }

    private fun parseJavaFile(javaFile: java.nio.file.Path, result: MutableSet<String>) {
        try {
            val fqn = parser.parseFullyQualifiedName(javaFile)
            if (fqn != null) {
                result.add(fqn)
            }
        } catch (e: IOException) {
            logger.debug("Failed to parse file: {}", javaFile, e)
        }
    }
}