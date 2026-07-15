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

import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

/**
 * Writes the auto-configuration import file for Spring Boot.
 * <p>
 * Generates a file containing fully qualified names of classes annotated with
 * {@code @AutoConfiguration}.
 * If the list of classes is empty, the output file is deleted to prevent stale entries.
 *
 * @author Vyacheslav Yanin
 * @see GenerateImportsByAnnotationTask
 */
final class AutoConfigFileWriter {

    private static final Logger log = Logging.getLogger(AutoConfigFileWriter.class);

    private static final String HEADER =
        "# Generated programmatically by scanning @AutoConfiguration annotation\n";

    public void write(File outputFile, List<String> classes) throws IOException {
        ensureParentDirectoryExists(outputFile);

        if (classes.isEmpty()) {
            deleteIfExists(outputFile);
            return;
        }

        String content = HEADER + String.join("\n", classes) + "\n";
        Files.writeString(outputFile.toPath(), content, StandardCharsets.UTF_8);

        log.lifecycle("Axelix Plugin: Found and registered {} @AutoConfiguration classes.",
            classes.size());
    }

    private void ensureParentDirectoryExists(File file) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            Files.createDirectories(parent.toPath());
        }
    }

    private void deleteIfExists(File file) throws IOException {
        if (file.exists()) {
            Files.delete(file.toPath());
        }
    }
}
