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
package com.axelixlabs.maven.plugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Ensures the Axelix spring-test-profiler entries are present in {@code META-INF/spring.factories}
 * on the test classpath. Any {@code spring.factories} already copied into the test output directory
 * is merged with the Axelix entries rather than overwritten.
 */
@Mojo(
        name = "prepare-test-profiler",
        defaultPhase = LifecyclePhase.PROCESS_TEST_RESOURCES,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true)
public class PrepareTestProfilerMojo extends AbstractMojo {

    private static final String SPRING_FACTORIES_PATH = "META-INF/spring.factories";

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoExecutionException {
        if (project.getBuild() == null) {
            return;
        }
        Path testOutput = Paths.get(project.getBuild().getTestOutputDirectory());
        Path target = testOutput.resolve(SPRING_FACTORIES_PATH);

        Map<String, String> existing = read(target);
        Map<String, String> merged = SpringFactoriesMerger.merge(existing);
        write(target, SpringFactoriesMerger.render(merged));
        getLog().info("Axelix: wrote spring-test-profiler entries to " + target);
    }

    private Map<String, String> read(Path target) throws MojoExecutionException {
        if (!Files.exists(target)) {
            return new LinkedHashMap<>();
        }
        try {
            String content = new String(Files.readAllBytes(target), StandardCharsets.UTF_8);
            return SpringFactoriesMerger.parse(content);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read " + target, e);
        }
    }

    private void write(Path target, String content) throws MojoExecutionException {
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write " + target, e);
        }
    }
}
