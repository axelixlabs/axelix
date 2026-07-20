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
import java.lang.classfile.Attributes
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassModel
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream

/**
 * Scan a single Java class to determine if it contains an
 * `@AutoConfiguration` annotation and extracts its fully qualified name.
 *
 * @author Vyacheslav Yanin
 */
internal class AutoConfigClassScanner {

    companion object {
        private val log = Logging.getLogger(AutoConfigClassScanner::class.java)
        private const val TARGET_ANNOTATION_DESCRIPTOR = "Lorg/springframework/boot/autoconfigure/AutoConfiguration;"
    }

    fun scanForAutoConfigurations(directories: Set<File>): List<String> {
        val classes = LinkedHashSet<String>()
        directories.forEach { dir ->
            if (!dir.exists()) {
                return@forEach
            }
            walkThroughDir(dir, classes)
        }
        return classes.toList()
    }

    private fun walkThroughDir(dir: File, classes: LinkedHashSet<String>) {
        try {
            Files.walk(dir.toPath()).use { stream: Stream<Path> ->
                stream.filter { Files.isRegularFile(it) }
                    .filter { it.toString().endsWith(".class") }
                    .forEach { classPath ->
                        addClassToAutoConfigListIfHasAnnotation(classPath, classes)
                    }
            }
        } catch (e: IOException) {
            log.warn("Failed to scan directory: $dir", e)
        }
    }

    private fun addClassToAutoConfigListIfHasAnnotation(classPath: Path, classes: LinkedHashSet<String>) {
        try {
            val bytes = Files.readAllBytes(classPath)
            val classFile = ClassFile.of()
            val model: ClassModel = classFile.parse(bytes)

            if (isHasAnnotation(model)) {
                val classDesc = model.thisClass()
                val className = classDesc.asSymbol().descriptorString()
                    .removePrefix("L")
                    .removeSuffix(";")
                    .replace('/', '.')
                classes.add(className)
            }
        } catch (e: Exception) {
            log.debug("Failed to process class: {}", classPath, e)
        }
    }

    private fun isHasAnnotation(model: ClassModel): Boolean {
        val annotationsAttr = model.findAttribute(Attributes.runtimeVisibleAnnotations())
            .orElse(null)

        val hasAnnotation = annotationsAttr?.annotations()?.any { annotation ->
            annotation.className().equalsString(TARGET_ANNOTATION_DESCRIPTOR)
        } ?: false
        return hasAnnotation
    }
}