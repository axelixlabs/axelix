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

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.artifacts.component.ModuleComponentIdentifier;
import org.gradle.api.artifacts.result.DependencyResult;
import org.gradle.api.artifacts.result.ResolutionResult;
import org.gradle.api.artifacts.result.ResolvedDependencyResult;
import org.jspecify.annotations.Nullable;

/**
 * Utility class for working with Gradle dependencies in various configurations.
 *
 * @author Mikhail Polivakha
 */
public class DependencyUtils {

    public static ModuleComponentIdentifier findInTheConfigurationClasspath(
            Project project, String configurationName, final String group, final String name) {

        // getAllDependencies (not getDependencies) so dependencies inherited from extended configurations
        // are seen too, e.g. a thymeleaf declared on implementation propagates to testRuntimeClasspath.
        DependencySet declaredDependencies =
                project.getConfigurations().getByName(configurationName).getAllDependencies();
        return findDeclaredInClasspath(getResolvedGraph(project, declaredDependencies), group, name);
    }

    private static ResolutionResult getResolvedGraph(Project project, DependencySet dependencies) {
        Configuration configurationCopy =
                project.getConfigurations().detachedConfiguration(dependencies.toArray(new Dependency[0]));

        return configurationCopy.getIncoming().getResolutionResult();
    }

    /**
     * Returns the dependency with the given group and name declared on the test runtime classpath,
     * including dependencies inherited from extended configurations (e.g. an {@code implementation}
     * dependency that propagates to {@code testRuntimeClasspath}), or {@code null} if none is
     * declared.
     */
    private static ModuleComponentIdentifier findDeclaredInClasspath(
            final ResolutionResult resolvedDependencies, final String group, final String name) {

        for (DependencyResult component : resolvedDependencies.getAllDependencies()) {
            if (component instanceof ResolvedDependencyResult) {

                ModuleComponentIdentifier gav = resolveDependencyGAV((ResolvedDependencyResult) component);

                if (gav != null
                        && gav.getGroup().equals(group)
                        && gav.getModule().equals(name)) {
                    return gav;
                }
            }
        }

        return null;
    }

    private static @Nullable ModuleComponentIdentifier resolveDependencyGAV(ResolvedDependencyResult component) {
        ComponentIdentifier dependencyIdentifier = component.getSelected().getId();

        if (dependencyIdentifier instanceof ModuleComponentIdentifier) {
            return (ModuleComponentIdentifier) dependencyIdentifier;
        } else {
            return null;
        }
    }
}
