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
import java.util.Optional;
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
 */
@Named("dependencyUtils")
@Singleton
public class DependencyUtils {
    private static final Logger log = LoggerFactory.getLogger(DependencyUtils.class);

    @Inject
    private ProjectDependenciesResolver resolver;

    private List<Artifact> resolvedDependencies = null;

    /**
     * Is project dependency present. Includes transitive
     *
     * @param groupId    dependency group id
     * @param artifactId dependency artifactId
     */
    public boolean containsDependency(
            MavenProject mavenProject, RepositorySystemSession repoSession, String groupId, String artifactId) {
        return getResolvedDependency(mavenProject, repoSession, groupId, artifactId)
                .isPresent();
    }

    /**
     * Get project dependency. Includes transitive
     *
     * @param groupId    dependency group id
     * @param artifactId dependency artifactId
     */
    public Optional<Artifact> getResolvedDependency(
            MavenProject mavenProject, RepositorySystemSession repoSession, String groupId, String artifactId) {
        if (resolvedDependencies == null) {
            resolveDependencies(mavenProject, repoSession);
        }

        return resolvedDependencies.stream()
                .filter(it ->
                        it.getGroupId().equals(groupId) && it.getArtifactId().equals(artifactId))
                .findAny();
    }

    public void resolveDependencies(MavenProject mavenProject, RepositorySystemSession repoSession) {
        DefaultDependencyResolutionRequest request = new DefaultDependencyResolutionRequest(mavenProject, repoSession);

        DependencyResolutionResult result;
        try {
            result = resolver.resolve(request);
        } catch (DependencyResolutionException e) {
            log.error("Failed to resolve dependencies");

            throw new RuntimeException(e);
        }

        resolvedDependencies =
                result.getDependencies().stream().map(Dependency::getArtifact).collect(Collectors.toList());
    }
}
