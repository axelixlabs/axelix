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
package com.axelixlabs.gradle.plugin;

import com.axelixlabs.axelix.common.utils.SemanticVersion;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedComponentResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;

/**
 * Contributes the Spring Test Profiler and Thymeleaf to the project's test classpath when they are
 * missing, leaving a project's own declarations untouched.
 */
final class TestDependencyContributor {

    static final String PROFILER_GROUP = "digital.pragmatech.testing";

    static final String PROFILER_NAME = "spring-test-profiler";

    static final String PROFILER_VERSION = "0.1.2";

    static final String PROFILER_DEPENDENCY = PROFILER_GROUP + ":" + PROFILER_NAME + ":" + PROFILER_VERSION;

    static final String THYMELEAF_GROUP = "org.thymeleaf";

    static final String THYMELEAF_NAME = "thymeleaf";

    static final String THYMELEAF_VERSION = "3.1.5.RELEASE";

    static final String THYMELEAF_DEPENDENCY = THYMELEAF_GROUP + ":" + THYMELEAF_NAME + ":" + THYMELEAF_VERSION;

    /** Minimum Thymeleaf version the profiler's HTML report renders on. */
    private static final SemanticVersion MIN_THYMELEAF_VERSION = SemanticVersion.parse("3.1.3");

    private TestDependencyContributor() {}

    /**
     * Adds the Spring Test Profiler to the test classpath. The profiler is added only
     * when it is not already declared.
     */
    static void contributeTestClasspathDependencies(final Project project, final DependencySet dependencies) {
        ResolutionResult resolvedGraph = getResolvedGraph(project, dependencies);

        if (!isOnClasspath(resolvedGraph, PROFILER_GROUP, PROFILER_NAME)) {
            project.getDependencies().add("testRuntimeOnly", PROFILER_DEPENDENCY);
        }
    }

    /**
     * Adds the Thymeleaf to the test classpath. Thymeleaf is added when it is absent and bumped when a
     * version older than {@code 3.1.3} is declared, because the profiler's HTML report fails to
     * render on older Thymeleaf; a project already on {@code 3.1.3} or newer is left untouched.
     */
    static void contributeCompileClasspathDependencies(final Project project, final DependencySet dependencies) {
        ResolutionResult resolvedGraph = getResolvedGraph(project, dependencies);

        if (shouldContributeThymeleaf(resolvedGraph)) {
            project.getDependencies().add("testImplementation", THYMELEAF_DEPENDENCY);
        }
    }

    private static ResolutionResult getResolvedGraph(Project project, DependencySet dependencies) {
        Configuration configurationCopy = project.getConfigurations().detachedConfiguration(
            dependencies.toArray(new Dependency[0])
        );

        return configurationCopy.getIncoming().getResolutionResult();
    }

    /**
     * Whether the plugin should put Thymeleaf {@code 3.1.3} on the test classpath: it does so when
     * Thymeleaf is not declared at all, or is declared at a version below {@code 3.1.3}. When an
     * older version is pinned directly, the contributed dependency wins by Gradle's newest-version
     * conflict resolution; a project already on {@code 3.1.3} or newer is left untouched.
     */
    private static boolean shouldContributeThymeleaf(final ResolutionResult resolvedDependencies) {
        ModuleComponentIdentifier declared = findDeclaredInClasspath(resolvedDependencies, THYMELEAF_GROUP, THYMELEAF_NAME);
        return declared == null || isBelowMinimumThymeleaf(declared.getVersion());
    }

    private static boolean isOnClasspath(final ResolutionResult resolvedDependencies, final String group, final String name) {
        return findDeclaredInClasspath(resolvedDependencies, group, name) != null;
    }

    /**
     * Returns the dependency with the given group and name declared on the test runtime classpath,
     * including dependencies inherited from extended configurations (e.g. an {@code implementation}
     * dependency that propagates to {@code testRuntimeClasspath}), or {@code null} if none is
     * declared.
     */
    private static ModuleComponentIdentifier findDeclaredInClasspath(
        final ResolutionResult resolvedDependencies,
        final String group,
        final String name
    ) {
        for (DependencyResult component : resolvedDependencies.getAllDependencies()) {
            if (!(component instanceof ResolvedDependencyResult))
                continue;

            ResolvedDependencyResult resolvedComponent = (ResolvedDependencyResult) component;
            ResolvedComponentResult selectedComponent = resolvedComponent.getSelected();

            ComponentIdentifier componentId = selectedComponent.getId();
            if (!(componentId instanceof ModuleComponentIdentifier))
                continue;

            ModuleComponentIdentifier moduleComponentId = (ModuleComponentIdentifier) componentId;
            if (!moduleComponentId.getGroup().equals(group) || !moduleComponentId.getModule().equals(name))
                continue;

            return moduleComponentId;
        }

        return null;
    }

    /**
     * Whether a declared Thymeleaf version is below {@code 3.1.3}, using {@link SemanticVersion} for
     * the comparison. Only the leading dot-separated numeric components are compared; a trailing
     * qualifier such as {@code .RELEASE} is stripped first, since {@link SemanticVersion} does not
     * accept it. A {@code null} or non-numeric version is treated as not below the minimum, leaving
     * an unrecognised version untouched.
     */
    private static boolean isBelowMinimumThymeleaf(final String version) {
        return SemanticVersion.parse(version).compareTo(MIN_THYMELEAF_VERSION) < 0;
    }
}
