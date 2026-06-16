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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Injects Axelix's recommended test-scoped dependencies into the building project when they are
 * missing from (or outdated on) the resolved test classpath:
 *
 * <ul>
 *   <li>{@code digital.pragmatech.testing:spring-test-profiler} — added when absent;</li>
 *   <li>{@code org.thymeleaf:thymeleaf} — added when absent or resolved below {@code thymeleafMinVersion}.</li>
 * </ul>
 *
 * <p>The goal binds to {@code initialize} and requires test-scope dependency resolution, so it can
 * inspect the fully resolved (transitive) classpath via {@link MavenProject#getArtifacts()} and add
 * the dependencies to the model before the test-compile and test phases run.
 *
 * <p>After resolving the dependencies the goal also registers a {@code META-INF/spring.factories} on
 * the test classpath (via a generated test resource) that wires spring-test-profiler's
 * {@code TestExecutionListener} and diagnostic {@code ApplicationContextInitializer}, merging with any
 * {@code spring.factories} the project already provides.
 */
@Mojo(
        name = "add-test-dependencies",
        defaultPhase = LifecyclePhase.INITIALIZE,
        requiresDependencyResolution = ResolutionScope.TEST,
        threadSafe = true)
public class AddTestDependenciesMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project}", readonly = true, required = true)
    private MavenProject project;

    @Parameter(property = "axelix.springTestProfiler.version", defaultValue = "0.1.2")
    private String springTestProfilerVersion;

    @Parameter(property = "axelix.thymeleaf.version", defaultValue = "3.1.5")
    private String thymeleafVersion;

    @Parameter(property = "axelix.thymeleaf.minVersion", defaultValue = "3.1.3")
    private String thymeleafMinVersion;

    @Parameter(property = "axelix.addTestDependencies.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() throws MojoExecutionException {
        if (skip) {
            getLog().info("axelix:add-test-dependencies skipped (axelix.addTestDependencies.skip=true)");
            return;
        }

        ResolvedClasspath classpath = new ResolvedClasspath(project.getArtifacts());
        TestDependencyPlanner planner =
                new TestDependencyPlanner(springTestProfilerVersion, thymeleafVersion, thymeleafMinVersion);

        List<Dependency> additions = planner.plan(classpath);
        if (additions.isEmpty()) {
            getLog().info("No test dependencies need to be added; classpath already satisfies the requirements.");
        } else {
            for (Dependency dependency : additions) {
                project.getDependencies().add(dependency);
                getLog().info(String.format(
                        "Added test dependency %s:%s:%s",
                        dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()));
            }
        }

        registerSpringFactories();
    }

    /**
     * Writes {@code META-INF/spring.factories} into a generated test-resources directory and registers
     * it on the project so {@code process-test-resources} copies it onto the test classpath. Any
     * {@code spring.factories} the project already declares on a test-resource root is merged in.
     */
    private void registerSpringFactories() throws MojoExecutionException {
        List<File> existing = new ArrayList<>();
        File rootDir = new File(project.getBuild().getDirectory(), "generated-test-resources/axelix");
        for (Resource resource : project.getTestResources()) {
            if (resource.getDirectory() == null || rootDir.getAbsolutePath().equals(resource.getDirectory())) {
                continue;
            }
            File candidate = new File(resource.getDirectory(), SpringFactoriesWriter.RELATIVE_PATH);
            if (candidate.isFile()) {
                existing.add(candidate);
            }
        }

        File written;
        try {
            written = new SpringFactoriesWriter().write(rootDir, existing);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write " + SpringFactoriesWriter.RELATIVE_PATH, e);
        }

        Resource resource = new Resource();
        resource.setDirectory(rootDir.getAbsolutePath());
        resource.addInclude(SpringFactoriesWriter.RELATIVE_PATH);
        resource.setFiltering(false);
        project.addTestResource(resource);

        getLog().info(String.format(
                "Registered Axelix %s on the test classpath at %s%s",
                SpringFactoriesWriter.RELATIVE_PATH,
                written,
                existing.isEmpty() ? "" : " (merged with existing project declarations)"));
    }
}
