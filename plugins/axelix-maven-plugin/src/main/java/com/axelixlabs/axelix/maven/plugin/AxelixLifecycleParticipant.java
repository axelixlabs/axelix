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
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The extension that wires up axelix-maven-plugin automatically for every {@link MavenProject
 * maven project} that declares it with {@code <extensions>true</extensions>} — no {@code
 * <executions>} needed.
 *
 * <p>It detects whether the Spring Boot Test Profiler is present anywhere in the reactor (e.g. a
 * shared {@code common} module that other modules depend on) and logs the result; it never adds
 * the dependency itself. {@code axelix-generate-project-info} is bound automatically on every
 * project that declares the plugin with {@code <extensions>true</extensions>} - a project that
 * declares the plugin but overrides that flag back to {@code false} is left untouched.
 *
 * @author Artemiy Degtyarev
 * @author Mikhail Polivakha
 */
@Named
@Singleton
public class AxelixLifecycleParticipant extends AbstractMavenLifecycleParticipant {

    private static final Logger log = LoggerFactory.getLogger(AxelixLifecycleParticipant.class);
    private static final String PLUGIN_KEY = "com.axelixlabs:axelix-maven-plugin";
    private static final String GENERATE_PROJECT_INFO_GOAL = "axelix-generate-project-info";

    public static final String PROFILER_DETECTED_PROPERTY = "spring.test.profiler.detected";

    @Inject
    SpringTestProfilerDetector springTestProfilerDetector;

    @Override
    public void afterProjectsRead(MavenSession session) {
        List<MavenProject> projects = session.getAllProjects();

        boolean profilerDetected = projects.stream()
                .anyMatch(project ->
                        springTestProfilerDetector.isProfilerPresent(project, session.getRepositorySession()));
        if (profilerDetected) {
            log.info("Spring Test Profiler detected in this reactor build");
        } else {
            log.info("Spring Test Profiler was not detected in this reactor build");
        }

        projects.forEach(project -> {
            project.getProperties().setProperty(PROFILER_DETECTED_PROPERTY, String.valueOf(profilerDetected));
            bindProjectInfoGoal(project);
        });
    }

    /**
     * {@code afterProjectsRead} runs for every project in the reactor, not just the ones that
     * declare {@code <extensions>true</extensions>} - so the {@link #PLUGIN_KEY} lookup must bail
     * out early for sibling modules that never declared axelix-maven-plugin at all, and also for
     * modules that declare it but override the inherited {@code <extensions>true</extensions>}
     * without opting back in, since auto-binding is documented as an extensions-only behavior.
     */
    private void bindProjectInfoGoal(MavenProject mavenProject) {
        Plugin plugin = mavenProject.getBuild().getPluginsAsMap().get(PLUGIN_KEY);
        if (plugin == null || !plugin.isExtensions()) {
            return;
        }

        addExecution(plugin);
    }

    private void addExecution(Plugin plugin) {
        PluginExecution execution = new PluginExecution();
        execution.setId("axelix-auto-" + GENERATE_PROJECT_INFO_GOAL);
        execution.setGoals(List.of(GENERATE_PROJECT_INFO_GOAL));
        plugin.addExecution(execution);
    }
}
