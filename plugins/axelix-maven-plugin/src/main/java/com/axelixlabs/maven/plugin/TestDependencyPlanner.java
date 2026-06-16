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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.Dependency;

/**
 * Pure decision logic that, given a project's {@link ResolvedClasspath}, computes which test-scoped
 * dependencies Axelix wants to inject. Currently it adds
 * {@code digital.pragmatech.testing:spring-test-profiler} when it is absent from the classpath.
 *
 * <p>This class deliberately depends only on the Maven model (not on the plugin runtime), so the
 * rules can be exercised by plain unit tests.
 */
final class TestDependencyPlanner {

    static final String TEST_SCOPE = "test";

    static final String SPRING_TEST_PROFILER_GROUP_ID = "digital.pragmatech.testing";
    static final String SPRING_TEST_PROFILER_ARTIFACT_ID = "spring-test-profiler";

    private final String springTestProfilerVersion;

    TestDependencyPlanner(String springTestProfilerVersion) {
        this.springTestProfilerVersion = springTestProfilerVersion;
    }

    /**
     * Returns the test-scoped dependencies that should be added to the project for the given
     * classpath, in declaration order. An empty list means nothing needs to be injected.
     */
    List<Dependency> plan(ResolvedClasspath classpath) {
        List<Dependency> additions = new ArrayList<>();

        if (classpath
                .versionOf(SPRING_TEST_PROFILER_GROUP_ID, SPRING_TEST_PROFILER_ARTIFACT_ID)
                .isEmpty()) {
            additions.add(testDependency(
                    SPRING_TEST_PROFILER_GROUP_ID, SPRING_TEST_PROFILER_ARTIFACT_ID, springTestProfilerVersion));
        }

        return additions;
    }

    private static Dependency testDependency(String groupId, String artifactId, String version) {
        Dependency dependency = new Dependency();
        dependency.setGroupId(groupId);
        dependency.setArtifactId(artifactId);
        dependency.setVersion(version);
        dependency.setScope(TEST_SCOPE);
        return dependency;
    }
}
