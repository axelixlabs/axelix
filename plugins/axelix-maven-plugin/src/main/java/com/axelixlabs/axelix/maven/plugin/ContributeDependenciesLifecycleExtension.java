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
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The extension that wires up axelix-maven-plugin automatically for every {@link MavenProject
 * maven project} that declares it with {@code <extensions>true</extensions>} — no {@code
 * <executions>} needed.
 *
 * <p>It contributes the Spring Boot Test Profiler dependency (and binds the goals that generate
 * its {@code spring.factories} and copy its HTML report), unless opted out via the {@value
 * #COPY_PROFILER_REPORT_PROPERTY} project property. {@code axelix-generate-project-info} is bound
 * unconditionally, regardless of that property.
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

    public static final String THYMELEAF_GROUP_ID = "org.thymeleaf";
    public static final String THYMELEAF_ARTIFACT_ID = "thymeleaf";

    /** Project property that mirrors the gradle plugin's {@code axelix { copyProfilerReport } }. */
    public static final String COPY_PROFILER_REPORT_PROPERTY = "axelix.copyProfilerReport";

    private static final String PLUGIN_KEY = "com.axelixlabs:axelix-maven-plugin";

    private static final Logger log = LoggerFactory.getLogger(ContributeDependenciesLifecycleExtension.class);

    @Inject
    DependencyResolver dependencyResolver;

    @Override
    public void afterProjectsRead(MavenSession session) {
        session.getAllProjects()
                .forEach(it ->
                        processProject(it, session.getRepositorySession(), isCopyProfilerReportEnabled(session, it)));
    }

    /**
     * Wires up a single project: binds axelix-maven-plugin's own goals, and contributes the
     * profiler dependency if it was requested and can actually be resolved.
     *
     * <p>{@code afterProjectsRead} runs for every project in the reactor, not just the one's that
     * declare {@code <extensions>true</extensions>} - so the {@link #PLUGIN_KEY} lookup must happen
     * first and bail out early, before touching dependencies. Otherwise, sibling modules that never
     * declared axelix-maven-plugin at all would still get the profiler dependency silently added.
     */
    private void processProject(
            MavenProject mavenProject, RepositorySystemSession repositorySystemSession, boolean profilerRequested) {
        Plugin plugin = mavenProject.getBuild().getPluginsAsMap().get(PLUGIN_KEY);
        if (plugin == null) {
            return;
        }

        // axelix-generate-project-info is bound unconditionally, regardless of profilerRequested,
        // and before the profiler starts work.
        addExecution(plugin, "axelix-generate-project-info");

        if (!profilerRequested) {
            log.info(
                    "Spring Test Profiler is disabled via the '{}' property; leaving the build untouched",
                    COPY_PROFILER_REPORT_PROPERTY);
            return;
        }

        try {
            if (contributeProfilerIfNecessary(mavenProject, repositorySystemSession)) {
                addExecution(plugin, "axelix-generate-spring-factories");
                addExecution(plugin, "axelix-copy-profiler-reports");
            }
        } catch (Exception e) {
            log.error(
                    "Axelix plugin encountered an error when running the extension for project '{}'. That "
                            + "means spring boot test profiler will not be added to this project",
                    mavenProject.getArtifactId(),
                    e);
        }
    }

    private void addExecution(Plugin plugin, String goal) {
        PluginExecution execution = new PluginExecution();
        execution.setId("axelix-auto-" + goal);
        execution.setGoals(List.of(goal));
        plugin.addExecution(execution);
    }

    /**
     * Reads the {@value #COPY_PROFILER_REPORT_PROPERTY} toggle, preferring a {@code -D} override
     * (a build's user properties) over the value declared in the project's own {@code
     * <properties>}, matching how Maven's own skip flags (e.g. {@code maven.test.skip}) behave.
     */
    private boolean isCopyProfilerReportEnabled(MavenSession session, MavenProject mavenProject) {
        String userValue = session.getUserProperties().getProperty(COPY_PROFILER_REPORT_PROPERTY);
        String value = userValue != null
                ? userValue
                : mavenProject.getProperties().getProperty(COPY_PROFILER_REPORT_PROPERTY, "true");
        return Boolean.parseBoolean(value);
    }

    /**
     * Contributes the profiler dependency if it is not already present.
     *
     * @param mavenProject Maven project
     * @param repositorySystemSession Maven repository system session
     * @return {@code true} if the profiler ends up present on the project's classpath (already there, or successfully
     *      added), {@code false} if it could not be resolved from the configured repositories (e.g. a closed network
     *      with no access to its repository) and was left out entirely.
     */
    boolean contributeProfilerIfNecessary(MavenProject mavenProject, RepositorySystemSession repositorySystemSession) {
        List<Artifact> artifacts = dependencyResolver.resolveDependencies(mavenProject, repositorySystemSession);
        Artifact springBootTestProfiler = findDependency(artifacts, PROFILER_ARTIFACT_ID, PROFILER_GROUP_ID);

        if (springBootTestProfiler != null) {
            log.info(
                    "Everything's good, Spring Boot Test profiler of version {} is already included in project {}",
                    springBootTestProfiler.getVersion(),
                    mavenProject.getArtifactId());
            return true;
        }

        Dependency profilerDependency = addSpringBootTestProfilerDependency(mavenProject);

        if (!dependencyResolver.isResolvable(mavenProject, repositorySystemSession)) {
            mavenProject.getDependencies().remove(profilerDependency);
            log.warn(
                    "Spring Test Profiler {} could not be resolved from the repositories configured for project "
                            + "'{}' (e.g. a closed network with no access to its repository); skipping it for this "
                            + "project",
                    PROFILER_VERSION,
                    mavenProject.getArtifactId());
            return false;
        }

        if (findDependency(artifacts, THYMELEAF_ARTIFACT_ID, THYMELEAF_GROUP_ID) == null) {
            log.info(
                    "Added the spring boot test profiler for the project {}; thymeleaf is not present, so it "
                            + "will be pulled in transitively by the profiler",
                    mavenProject.getArtifactId());
        } else {
            log.info(
                    "Added just the spring boot test profiler for the project {}; thymeleaf is already present "
                            + "in classpath",
                    mavenProject.getArtifactId());
        }
        return true;
    }

    private Dependency addSpringBootTestProfilerDependency(MavenProject mavenProject) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(PROFILER_GROUP_ID);
        dependency.setArtifactId(PROFILER_ARTIFACT_ID);
        dependency.setVersion(PROFILER_VERSION);
        dependency.setScope("test");

        mavenProject.getDependencies().add(dependency);
        return dependency;
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
