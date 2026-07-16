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
package com.axelixlabs.axelix.maven.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Mojo that copies {@code spring-test-profiler} reports to application classpath
 *
 * @author Artemiy Degtyarev
 */
@Mojo(name = "axelix-copy-profiler-reports", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class CopyProfilerReportsMojo extends AbstractMojo {
    @Parameter(readonly = true, defaultValue = "${project}")
    @SuppressWarnings("NullAway")
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        Path reportsPath = Path.of(mavenProject.getBuild().getDirectory(), "spring-test-profiler");
        Path target = Path.of(mavenProject.getBuild().getOutputDirectory(), "spring-test-profiler");

        if (Files.notExists(reportsPath)) {
            return;
        }

        try (Stream<Path> pathStream = Files.walk(reportsPath)) {
            pathStream.forEach(path -> {
                Path dest = target.resolve(reportsPath.relativize(path));

                if (Files.isDirectory(path)) {
                    try {
                        Files.createDirectories(dest);
                    } catch (IOException e) {
                        getLog().error("Failed to create directory when copying reports");
                        throw new RuntimeException(e);
                    }
                    return;
                }

                try {
                    Files.copy(path, dest, StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    getLog().error("Failed to copy profiler-report file");
                    throw new RuntimeException(e);
                }
            });
        } catch (IOException e) {
            getLog().error("Failed to open stream to reports folder");
            throw new RuntimeException(e);
        }
    }
}
