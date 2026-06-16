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
import java.util.Optional;

import org.apache.maven.artifact.versioning.ComparableVersion;
import org.apache.maven.model.Dependency;

/**
 * Pure decision logic that, given a project's {@link ResolvedClasspath}, computes which test-scoped
 * dependencies Axelix wants to inject:
 *
 * <ul>
 *   <li>{@code digital.pragmatech.testing:spring-test-profiler} when it is absent;</li>
 *   <li>{@code org.thymeleaf:thymeleaf} when it is absent or resolved below a minimum version.</li>
 * </ul>
 *
 * <p>This class deliberately depends only on the Maven model + versioning utilities (not on the
 * plugin runtime), so the rules can be exercised by plain unit tests.
 */
final class TestDependencyPlanner {

    static final String TEST_SCOPE = "test";

    static final String SPRING_TEST_PROFILER_GROUP_ID = "digital.pragmatech.testing";
    static final String SPRING_TEST_PROFILER_ARTIFACT_ID = "spring-test-profiler";

    static final String THYMELEAF_GROUP_ID = "org.thymeleaf";
    static final String THYMELEAF_ARTIFACT_ID = "thymeleaf";

    private final String springTestProfilerVersion;
    private final String thymeleafVersion;
    private final ComparableVersion thymeleafMinVersion;

    TestDependencyPlanner(String springTestProfilerVersion, String thymeleafVersion, String thymeleafMinVersion) {
        this.springTestProfilerVersion = springTestProfilerVersion;
        this.thymeleafVersion = thymeleafVersion;
        this.thymeleafMinVersion = new ComparableVersion(thymeleafMinVersion);
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

        if (thymeleafNeedsUpgrade(classpath.versionOf(THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID))) {
            additions.add(testDependency(THYMELEAF_GROUP_ID, THYMELEAF_ARTIFACT_ID, thymeleafVersion));
        }

        return additions;
    }

    private boolean thymeleafNeedsUpgrade(Optional<String> resolvedVersion) {
        if (resolvedVersion.isEmpty()) {
            return true;
        }
        return new ComparableVersion(resolvedVersion.get()).compareTo(thymeleafMinVersion) < 0;
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
