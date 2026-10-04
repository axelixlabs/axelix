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

import javax.inject.Inject;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;

/**
 * Umbrella mojo that serves as the entrypoint to all stuff Axelix plugin is supposed to do during the build.
 *
 * @author Nikita Kirillov
 * @author Mikhail Polivakha
 */
@Mojo(name = "axelix-generate-project-info", defaultPhase = LifecyclePhase.PREPARE_PACKAGE)
public class GenerateProjectInfoMojo extends AbstractMojo {

    @Parameter(readonly = true, defaultValue = "${project}")
    @SuppressWarnings("NullAway")
    private MavenProject mavenProject;

    @Parameter(readonly = true, defaultValue = "${repositorySystemSession}")
    @SuppressWarnings("NullAway")
    private RepositorySystemSession repositorySystemSession;

    @Parameter(readonly = true, defaultValue = "${session}")
    @SuppressWarnings("NullAway")
    private MavenSession mavenSession;

    @Inject
    @SuppressWarnings("NullAway")
    private ProjectInfoGenerator projectInfoGenerator;

    @Inject
    @SuppressWarnings("NullAway")
    private DependenciesSbomGenerator dependenciesSbomGenerator;

    @Override
    public void execute() throws MojoExecutionException {
        projectInfoGenerator.generate(mavenProject, repositorySystemSession);
        dependenciesSbomGenerator.generate(mavenProject, mavenSession, repositorySystemSession);
    }
}
