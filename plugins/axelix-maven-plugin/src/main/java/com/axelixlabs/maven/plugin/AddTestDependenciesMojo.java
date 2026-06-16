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

import java.util.List;

import org.apache.maven.model.Dependency;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

/**
 * Injects Axelix's recommended test-scoped dependencies into the building project when they are
 * missing from the resolved test classpath. Currently it adds
 * {@code digital.pragmatech.testing:spring-test-profiler} when it is absent.
 *
 * <p>The goal binds to {@code initialize} and requires test-scope dependency resolution, so it can
 * inspect the fully resolved (transitive) classpath via {@link MavenProject#getArtifacts()} and add
 * the dependencies to the model before the test-compile and test phases run.
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

    @Parameter(property = "axelix.addTestDependencies.skip", defaultValue = "false")
    private boolean skip;

    @Override
    public void execute() {
        if (skip) {
            getLog().info("axelix:add-test-dependencies skipped (axelix.addTestDependencies.skip=true)");
            return;
        }

        ResolvedClasspath classpath = new ResolvedClasspath(project.getArtifacts());
        TestDependencyPlanner planner = new TestDependencyPlanner(springTestProfilerVersion);

        List<Dependency> additions = planner.plan(classpath);
        if (additions.isEmpty()) {
            getLog().info("No test dependencies need to be added; classpath already satisfies the requirements.");
            return;
        }

        for (Dependency dependency : additions) {
            project.getDependencies().add(dependency);
            getLog().info(String.format(
                    "Added test dependency %s:%s:%s",
                    dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion()));
        }
    }
}
