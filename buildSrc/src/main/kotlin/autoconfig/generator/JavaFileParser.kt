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

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Parses a single Java source file to determine if it contains an
 * `@AutoConfiguration` annotation and extracts its fully qualified name.
 *
 * @author Vyacheslav Yanin
 */
internal class JavaFileParser {

    @Throws(IOException::class)
    fun parseFullyQualifiedName(javaFile: Path): String? {
        val lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8)

        val packageName = extractPackageName(lines)
        if (packageName.isEmpty() || !containsAutoConfigurationAnnotation(lines)) {
            return null
        }
        val className = getClassNameFromFile(javaFile)
        return "$packageName.$className"
    }

    private fun extractPackageName(lines: List<String>): String {
        for (line in lines) {
            val trimmed = line.trim()
            if (isCommentRow(trimmed)) {
                continue
            }

            if (trimmed.startsWith("package ") && trimmed.contains(";")) {
                val packageLine = trimmed.substringBefore(";")
                if (packageLine.length > 8) {
                    return packageLine.substring(8).trim()
                }
            }

            // If class declaration reached without @AutoConfiguration - this class isn't an auto-config candidate
            if (trimmed.matches(ANY_CLASS_HEADER_REGEX)) {
                break
            }
        }
        return ""
    }

    private fun isCommentRow(trimmed: String): Boolean = trimmed.isEmpty() ||
            trimmed.startsWith("//") ||
            trimmed.startsWith("/*") ||
            trimmed.startsWith("*")

    private fun containsAutoConfigurationAnnotation(lines: List<String>): Boolean {
        for (line in lines) {
            val trimmed = line.trim()
            if (isCommentRow(trimmed)) {
                continue
            }
            if (trimmed.contains(TARGET_ANNOTATION)) {
                return true
            }
        }
        return false
    }

    private fun getClassNameFromFile(javaFile: Path): String {
        val fileName = javaFile.fileName.toString()
        return fileName.dropLast(5) // ".java"
    }

    companion object {
        private const val TARGET_ANNOTATION = "@AutoConfiguration"
        private val ANY_CLASS_HEADER_REGEX = Regex("^(public|private|protected)?\\s*(class|interface|@interface|enum)\\s+")
    }
}