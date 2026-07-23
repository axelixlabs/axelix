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

import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.project.DefaultDependencyResolutionRequest;
import org.apache.maven.project.DependencyResolutionException;
import org.apache.maven.project.DependencyResolutionResult;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectDependenciesResolver;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utilities for working with dependencies
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@Named
@Singleton
public class DependencyResolver {

    private static final Logger log = LoggerFactory.getLogger(DependencyResolver.class);

    @Inject
    private ProjectDependenciesResolver resolver;

    /**
     * Resolve maven project dependencies.
     *
     * @param mavenProject maven project
     * @param repoSession maven repository session
     * @return List of project dependencies
     */
    public List<Artifact> resolveDependencies(MavenProject mavenProject, RepositorySystemSession repoSession) {
        DefaultDependencyResolutionRequest request = new DefaultDependencyResolutionRequest(mavenProject, repoSession);

        DependencyResolutionResult result;
        try {
            result = resolver.resolve(request);
        } catch (DependencyResolutionException e) {
            log.error(
                    "Axelix maven plugin is not capable to resolve the dependency tree of project '{}'. This is "
                            + "critical and the plugin cannot work without it. Make sure your repositories are "
                            + "declared correctly and you have a stable internet connection",
                    mavenProject.getArtifactId(),
                    e);

            throw new RuntimeException(e);
        }

        return result.getDependencies().stream().map(Dependency::getArtifact).collect(Collectors.toList());
    }

    /**
     * Probes whether the project's dependencies can be resolved from the configured repositories,
     * without the noisy error-level logging {@link #resolveDependencies} does: a {@code false}
     * result here is an expected, handled outcome for callers deciding whether to keep a
     * tentatively-added dependency.
     */
    public boolean isResolvable(MavenProject mavenProject, RepositorySystemSession repoSession) {
        DefaultDependencyResolutionRequest request = new DefaultDependencyResolutionRequest(mavenProject, repoSession);

        try {
            resolver.resolve(request);
            return true;
        } catch (DependencyResolutionException e) {
            log.debug("Dependency resolution probe failed for project '{}'", mavenProject.getArtifactId(), e);
            return false;
        }
    }
}
