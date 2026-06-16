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

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.project.MavenProject;

/**
 * Injects the test-scoped dependencies the spring-test-profiler diagnostics rely on into every
 * reactor project that declares the Axelix Maven plugin.
 *
 * <p>This runs as a Maven lifecycle participant (registered via {@code META-INF/plexus/components.xml})
 * so it executes in {@link #afterProjectsRead(MavenSession)}, before dependency resolution. Mutating
 * dependencies here is what reliably gets them onto the Surefire test classpath; doing the same from
 * a phase-bound Mojo would be too late. It is only active when the plugin is declared with
 * {@code <extensions>true</extensions>}.
 */
public class TestProfilerDependencyInjector extends AbstractMavenLifecycleParticipant {

    private static final String PLUGIN_GROUP_ID = "com.axelixlabs";
    private static final String PLUGIN_ARTIFACT_ID = "axelix-maven-plugin";

    private static final String PROFILER_GROUP_ID = "digital.pragmatech.testing";
    private static final String PROFILER_ARTIFACT_ID = "spring-test-profiler";
    private static final String PROFILER_VERSION_PROPERTY = "axelix.spring-test-profiler.version";
    private static final String PROFILER_DEFAULT_VERSION = "0.1.2";

    private static final String THYMELEAF_GROUP_ID = "org.thymeleaf";
    private static final String THYMELEAF_ARTIFACT_ID = "thymeleaf";

    private static final String TEST_SCOPE = "test";

    @Override
    public void afterProjectsRead(MavenSession session) {
        for (MavenProject project : session.getAllProjects()) {
            if (declaresAxelixPlugin(project)) {
                ensureProfilerDependency(project);
                ensureThymeleafFloor(project);
            }
        }
    }

    private boolean declaresAxelixPlugin(MavenProject project) {
        return project.getBuildPlugins().stream()
                .anyMatch(plugin -> PLUGIN_GROUP_ID.equals(plugin.getGroupId())
                        && PLUGIN_ARTIFACT_ID.equals(plugin.getArtifactId()));
    }

    private void ensureProfilerDependency(MavenProject project) {
        if (findDependency(project, PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID) != null) {
            return;
        }
        String version = project.getProperties().getProperty(PROFILER_VERSION_PROPERTY, PROFILER_DEFAULT_VERSION);
        project.getDependencies().add(dependency(PROFILER_GROUP_ID, PROFILER_ARTIFACT_ID, version, TEST_SCOPE));
    }

    /**
     * Ensures Thymeleaf is at least {@link ThymeleafVersionPolicy#FLOOR} on the test classpath. When
     * Thymeleaf is absent a test-scoped dependency at the floor is added; when it is present at a lower
     * version its version is raised in place while keeping the existing scope, so a runtime usage of
     * Thymeleaf in the consumer application is not disturbed.
     */
    private void ensureThymeleafFloor(MavenProject project) {
        Dependency existing = findDependency(project, THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID);
        if (existing == null) {
            project.getDependencies()
                    .add(dependency(
                            THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID, ThymeleafVersionPolicy.FLOOR, TEST_SCOPE));
            return;
        }
        String version = existing.getVersion();
        if (version == null) {
            version = managedVersion(project, THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID);
        }
        if (ThymeleafVersionPolicy.isBelowFloor(version)) {
            existing.setVersion(ThymeleafVersionPolicy.FLOOR);
        }
    }

    private Dependency findDependency(MavenProject project, String groupId, String artifactId) {
        return project.getDependencies().stream()
                .filter(dependency ->
                        groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId()))
                .findFirst()
                .orElse(null);
    }

    private String managedVersion(MavenProject project, String groupId, String artifactId) {
        DependencyManagement management = project.getDependencyManagement();
        if (management == null) {
            return null;
        }
        return management.getDependencies().stream()
                .filter(dependency ->
                        groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId()))
                .map(Dependency::getVersion)
                .findFirst()
                .orElse(null);
    }

    private Dependency dependency(String groupId, String artifactId, String version, String scope) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setScope(scope);
        return dependency;
    }
}
