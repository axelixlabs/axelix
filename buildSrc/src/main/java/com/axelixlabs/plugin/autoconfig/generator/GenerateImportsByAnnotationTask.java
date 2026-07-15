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

import org.gradle.api.DefaultTask;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.tasks.CacheableTask;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

/**
 * A custom Gradle task that performs a lightweight static text scan of Java source files
 * to find classes annotated with {@code @AutoConfiguration}.
 * <p>
 * This task parse source files directly as raw text. It extracts the full package structure and the class name, then generates
 * or updates the Spring Boot configuration imports file inside the source resources directory.
 * </p>
 *
 * @author Vyacheslav Yanin
 * @see AxelixAutoConfigPlugin
 * @see AutoConfigFileWriter
 * @see JavaFileParser
 */
@CacheableTask
public abstract class GenerateImportsByAnnotationTask extends DefaultTask {

    private static final Logger log = Logging.getLogger(GenerateImportsByAnnotationTask.class);

    private final JavaFileParser parser = new JavaFileParser();
    private final AutoConfigFileWriter writer = new AutoConfigFileWriter();

    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSourceDirectories();

    @OutputFile
    public abstract RegularFileProperty getOutputFile();

    @TaskAction
    public void generate() throws IOException {
        List<String> autoConfigClasses = scanForAutoConfigurations();
        writer.write(getOutputFile().get().getAsFile(), autoConfigClasses);
    }

    private List<String> scanForAutoConfigurations() {
        Set<String> classes = new LinkedHashSet<>();
        for (File sourceDir : getSourceDirectories().getFiles()) {
            if (!sourceDir.isDirectory()) continue;

            try (Stream<Path> walk = Files.walk(sourceDir.toPath())) {
                walk.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> parseJavaFile(path, classes));
            } catch (IOException e) {
                log.warn("Failed to scan directory: {}", sourceDir, e);
            }
        }
        return new ArrayList<>(classes);
    }

    private void parseJavaFile(Path javaFile, Set<String> result) {
        try {
            String fqn = parser.parseFullyQualifiedName(javaFile);
            if (fqn != null) {
                result.add(fqn);
            }
        } catch (IOException e) {
            log.debug("Failed to parse file: {}", javaFile, e);
        }
    }
}
