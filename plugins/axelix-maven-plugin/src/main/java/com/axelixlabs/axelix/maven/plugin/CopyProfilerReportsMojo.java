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

    private static final int MINIMUM_JAVA_VERSION = 17;

    @Parameter(readonly = true, defaultValue = "${project}")
    @SuppressWarnings("NullAway")
    private MavenProject mavenProject;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (!JavaCompatibility.compilesToAtLeast(mavenProject, MINIMUM_JAVA_VERSION)) {
            getLog().info("Project targets Java below " + MINIMUM_JAVA_VERSION + ". Skipping profiler report copy.");
            return;
        }

        Path sourceFile = Path.of(mavenProject.getBuild().getDirectory(), "spring-test-profiler", "latest.html");
        Path targetDir = Path.of(mavenProject.getBuild().getOutputDirectory(), "META-INF", "axelix");
        Path targetFile = targetDir.resolve(sourceFile.getFileName());

        if (Files.notExists(sourceFile)) {
            getLog().info("Profiler report 'latest.html' not found. Skipping copy.");
            return;
        }

        try {
            Files.createDirectories(targetDir);

            Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);
            getLog().info("Copied profiler report to " + targetFile);

        } catch (IOException e) {
            throw new MojoExecutionException("Failed to copy latest.html to target resources", e);
        }
    }
}
