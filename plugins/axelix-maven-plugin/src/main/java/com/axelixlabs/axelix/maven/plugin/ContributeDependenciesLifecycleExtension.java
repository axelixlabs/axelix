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

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.utils.SemanticVersion;

/**
 * The extension that has a goal to contribute the Spring Boot Test Profiler dependency
 * to all the {@link MavenProject maven projects} (both in case of multi-module maven build
 * and simple maven project build).
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@Named
@Singleton
public class ContributeDependenciesLifecycleExtension extends AbstractMavenLifecycleParticipant {

    // TODO: this information is duplicated in both gradle/maven plugins. We can and should extract it.
    public static final String PROFILER_GROUP_ID = "digital.pragmatech.testing";
    public static final String PROFILER_ARTIFACT_ID = "spring-test-profiler";
    public static final String PROFILER_VERSION = "0.1.2";

    public static final SemanticVersion MIN_THYMELEAF_VERSION = SemanticVersion.parse("3.1.3");
    public static final String THYMELEAF_GROUP_ID = "org.thymeleaf";
    public static final String THYMELEAF_ARTIFACT_ID = "thymeleaf";
    public static final String THYMELEAF_VERSION = "3.1.5.RELEASE";

    private static final Logger log = LoggerFactory.getLogger(ContributeDependenciesLifecycleExtension.class);

    @Inject
    private DependencyResolver dependencyResolver;

    @Override
    public void afterProjectsRead(MavenSession session) {
        try {
            session.getAllProjects().forEach(it -> contributeProfilerIfNecessary(it, session.getRepositorySession()));
        } catch (Exception e) {
            log.error(
                    "Axelix plugin encountered an error when running the extension. That means spring boot test profiler will not be added",
                    e);
        }
    }

    /**
     * Contribute project dependencies
     *
     * @param mavenProject Maven project
     * @param repositorySystemSession Maven repository system session
     */
    private void contributeProfilerIfNecessary(
            MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {

        List<Artifact> artifacts = dependencyResolver.resolveDependencies(mavenProject, repositorySystemSession);

        Artifact springBootTestProfiler = findDependency(artifacts, PROFILER_ARTIFACT_ID, PROFILER_GROUP_ID);

        if (springBootTestProfiler == null) {
            continueProcessing(mavenProject, artifacts);
        } else {
            log.info(
                    "Maven project {} already contains spring boot test profiler. No action is taken",
                    mavenProject.getArtifact().getArtifactId());
        }
    }

    private void continueProcessing(MavenProject mavenProject, List<Artifact> artifacts) {
        Artifact thymeleaf = findDependency(artifacts, THYMELEAF_ARTIFACT_ID, THYMELEAF_GROUP_ID);

        if (thymeleaf == null) {
            addBoth(mavenProject);
        } else {
            if (SemanticVersion.parse(thymeleaf.getVersion()).isAtLeast(MIN_THYMELEAF_VERSION)) {
                addProfilerOnly(mavenProject);
            } else {
                log.warn(
                        "Unable to add spring boot test profiler for the project. The thymeleaf of version {} "
                                + "is already present in classpath. This version is not compatible with profiler. For classpath-safety "
                                + "reasons, we're leaving your dependency as it is. But the profiler is not going to be introduced.",
                        thymeleaf.getVersion());
            }
        }
    }

    private void addProfilerOnly(MavenProject mavenProject) {
        addSpringBootTestProfilerDependency(mavenProject);
        log.info(
                "Successfully added the spring boot test profiler for the project {}. "
                        + "Thymeleaf of compatible version was already present in classpath",
                mavenProject.getArtifact().getArtifactId());
    }

    private void addBoth(MavenProject mavenProject) {
        addThymeleafDependency(mavenProject);
        addSpringBootTestProfilerDependency(mavenProject);
        log.info(
                "Successfully added the spring boot test profiler and thymeleaf dependencies for the project {}",
                mavenProject.getArtifact().getArtifactId());
    }

    private void addThymeleafDependency(MavenProject mavenProject) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(THYMELEAF_GROUP_ID);
        dependency.setArtifactId(THYMELEAF_ARTIFACT_ID);
        dependency.setVersion(THYMELEAF_VERSION);
        dependency.setScope("test");

        mavenProject.getDependencies().add(dependency);
    }

    private void addSpringBootTestProfilerDependency(MavenProject mavenProject) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(PROFILER_GROUP_ID);
        dependency.setArtifactId(PROFILER_ARTIFACT_ID);
        dependency.setVersion(PROFILER_VERSION);
        dependency.setScope("test");

        mavenProject.getDependencies().add(dependency);
    }

    public @Nullable Artifact findDependency(List<Artifact> dependencies, String artifactId, String groupId) {

        for (Artifact dependency : dependencies) {
            if (dependency.getGroupId().equals(groupId)
                    && dependency.getArtifactId().equals(artifactId)) {
                return dependency;
            }
        }

        return null;
    }
}
