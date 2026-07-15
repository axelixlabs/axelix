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

import org.gradle.api.logging.Logging
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * Writes the auto-configuration import file for Spring Boot.
 *
 * Generates a file containing fully qualified names of classes annotated with
 * `@AutoConfiguration`.
 * If the list of classes is empty, the output file is deleted to prevent stale entries.
 *
 * @author Vyacheslav Yanin
 * @see GenerateImportsByAnnotationTask
 */
internal class AutoConfigFileWriter {

    @Throws(IOException::class)
    fun write(outputFile: File, classes: List<String>) {
        ensureParentDirectoryExists(outputFile)

        if (classes.isEmpty()) {
            Files.deleteIfExists(outputFile.toPath())
            return
        }

        val content = HEADER + classes.joinToString("\n") + "\n"
        Files.writeString(outputFile.toPath(), content, StandardCharsets.UTF_8)
        log.lifecycle("Axelix Plugin: Found and registered {} @AutoConfiguration classes.", classes.size)
    }

    @Throws(IOException::class)
    private fun ensureParentDirectoryExists(file: File) {
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath())
        }
    }

    companion object {
        private val log = Logging.getLogger(AutoConfigFileWriter::class.java)
        private const val HEADER = "# Generated programmatically by scanning @AutoConfiguration annotation\n"
    }
}