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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Parses a single Java source file to determine if it contains an
 * {@code @AutoConfiguration} annotation and extracts its fully qualified name.
 *
 * @author Vyacheslav Yanin
 */
final class JavaFileParser {

    private static final String TARGET_ANNOTATION = "@AutoConfiguration";

    public String parseFullyQualifiedName(Path javaFile) throws IOException {
        List<String> lines = Files.readAllLines(javaFile, StandardCharsets.UTF_8);

        String packageName = extractPackageName(lines);
        if (packageName.isEmpty() || !containsAutoConfigurationAnnotation(lines)) {
            return null;
        }
        String className = getClassNameFromFile(javaFile);
        return packageName + "." + className;
    }

    private String extractPackageName(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("package ") && trimmed.endsWith(";")) {
                return trimmed.substring(8, trimmed.length() - 1).trim();
            }
            // Class declaration reached without @AutoConfiguration - this class isn't an auto-config candidate
            if (trimmed.matches("^(public|private|protected)?\\s*(class|interface|@interface|enum)\\s+")) {
                break;
            }
        }
        return "";
    }

    private boolean containsAutoConfigurationAnnotation(List<String> lines) {
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("/*")) {
                continue;
            }
            if (trimmed.contains(TARGET_ANNOTATION)) {
                return true;
            }
        }
        return false;
    }

    private String getClassNameFromFile(Path javaFile) {
        String fileName = javaFile.getFileName().toString();
        return fileName.substring(0, fileName.length() - 5); // ".java"
    }
}
