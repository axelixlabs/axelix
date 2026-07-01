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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.DependencySet;

/**
 * Applies dependencies to runtime classpath
 *
 * @author Artemiy Degtyarev
 */
public class TestClasspathDependencyApplier implements Action<DependencySet> {
    private final Project project;

    public TestClasspathDependencyApplier(Project project) {
        this.project = project;
    }

    @Override
    public void execute(DependencySet dependencies) {
        TestDependencyContributor.contributeTestClasspathDependencies(project, dependencies);
    }
}
