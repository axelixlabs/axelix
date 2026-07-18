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

    private fun addClassToAutoConfigListIfHasAnnotation(classPath: Path?, classes: LinkedHashSet<String>) {
        try {
            val classFile = ClassFile.of()
            val model = classFile.parse(classPath)

            if (hasAutoConfigurationAnnotation(model)) {
                val className = extractClassName(model)
                if (className != null) {
                    classes.add(className)
                }
            }
        } catch (e: Exception) {
            log.debug("Failed to process class: {}", classPath, e)
        }
    }

    private fun hasAutoConfigurationAnnotation(model: ClassModel): Boolean {
        return try {
            val annotationsAttr = model.findAttribute(Attributes.runtimeVisibleAnnotations())
                .orElse(null)

            annotationsAttr?.annotations()?.any { annotation ->
                annotation.classSymbol().descriptorString() == TARGET_ANNOTATION_DESCRIPTOR
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    private fun extractClassName(model: ClassModel): String? {
        return try {
            val sym = model.thisClass().asSymbol()
            val pkg = sym.packageName()
            val name = sym.displayName()

            if (pkg.isEmpty()) name else "$pkg.$name"
        } catch (_: Exception) {
            null
        }
    }
}