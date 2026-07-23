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

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Detects whether the Spring Boot Test Profiler is already present on a project's classpath; it
 * never adds the dependency itself.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 * @author Nikita Kirillov
 */
@Named
@Singleton
public class SpringTestProfilerDetector {

    private static final Logger log = LoggerFactory.getLogger(SpringTestProfilerDetector.class);

    // TODO: this information is duplicated in both gradle/maven plugins. We can and should extract it.
    public static final String PROFILER_GROUP_ID = "digital.pragmatech.testing";
    public static final String PROFILER_ARTIFACT_ID = "spring-test-profiler";

    @Inject
    DependencyResolver dependencyResolver;

    /**
     * Resolution failures are treated as "not present" rather than propagated - callers that scan
     * many projects (e.g. a whole reactor) rely on one project's broken dependency tree not taking
     * down detection for the rest.
     */
    public boolean isProfilerPresent(MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {
        List<Artifact> artifacts;
        try {
            artifacts = dependencyResolver.resolveDependencies(mavenProject, repositorySystemSession);
        } catch (Exception e) {
            log.debug(
                    "Could not resolve dependencies of project '{}' for profiler detection",
                    mavenProject.getArtifactId(),
                    e);
            return false;
        }

        return findDependency(artifacts) != null;
    }

    private @Nullable Artifact findDependency(List<Artifact> dependencies) {
        for (Artifact dependency : dependencies) {
            if (dependency.getGroupId().equals(PROFILER_GROUP_ID)
                    && dependency.getArtifactId().equals(PROFILER_ARTIFACT_ID)) {
                return dependency;
            }
        }
        return null;
    }
}
