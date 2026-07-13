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

import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;

import com.axelixlabs.axelix.common.utils.SemanticVersion;

/**
 * Contributes dependencies to maven project
 */
@Named("contributeDependenciesLifecycleExtension")
@Singleton
public class ContributeDependenciesLifecycleExtension extends AbstractMavenLifecycleParticipant {
    public static final String PROFILER_GROUP_ID = "digital.pragmatech.testing";
    public static final String PROFILER_ARTIFACT_ID = "spring-test-profiler";
    public static final String PROFILER_VERSION = "0.1.2";
    public static final SemanticVersion MIN_THYMELEAF_VERSION = SemanticVersion.parse("3.1.3");
    public static final String THYMELEAF_GROUP_ID = "org.thymeleaf";
    public static final String THYMELEAF_ARTIFACT_ID = "thymeleaf";
    public static final String THYMELEAF_VERSION = "3.1.5.RELEASE";

    @Inject
    private DependencyUtils dependencyUtils;

    @Override
    public void afterProjectsRead(MavenSession session) {
        session.getAllProjects().forEach(it -> contributeProjectDependencies(it, session.getRepositorySession()));
    }

    private void contributeProjectDependencies(
            MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {
        contributeProfilerDependency(mavenProject, repositorySystemSession);
        contributeThymeleafDependency(mavenProject, repositorySystemSession);
    }

    private void contributeThymeleafDependency(
            MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {
        Optional<Artifact> dependency = dependencyUtils.getResolvedDependency(
                mavenProject, repositorySystemSession, THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID);

        if (dependency.isEmpty()) {
            return;
        }

        Artifact artifact = dependency.get();

        if (SemanticVersion.parse(artifact.getVersion()).compareTo(MIN_THYMELEAF_VERSION) < 0) {
            addThymeleafDependency(mavenProject);
        }
    }

    private void addThymeleafDependency(MavenProject mavenProject) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(THYMELEAF_GROUP_ID);
        dependency.setArtifactId(THYMELEAF_ARTIFACT_ID);
        dependency.setVersion(THYMELEAF_VERSION);

        mavenProject.getDependencies().add(dependency);
    }

    private void contributeProfilerDependency(
            MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {
        if (!dependencyUtils.containsDependency(
                mavenProject, repositorySystemSession, PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID)) {
            Dependency dependency = new Dependency();
            dependency.setGroupId(PROFILER_GROUP_ID);
            dependency.setArtifactId(PROFILER_ARTIFACT_ID);
            dependency.setVersion(PROFILER_VERSION);
            dependency.setScope("test");

            mavenProject.getDependencies().add(dependency);
        }
    }
}
