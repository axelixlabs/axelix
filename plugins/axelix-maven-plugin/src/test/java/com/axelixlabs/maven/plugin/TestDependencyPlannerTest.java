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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TestDependencyPlannerTest {

    private static final String PROFILER_VERSION = "0.1.2";

    private final TestDependencyPlanner subject = new TestDependencyPlanner(PROFILER_VERSION);

    @Test
    void addsProfilerWhenAbsent() {
        // given
        ResolvedClasspath classpath = classpathOf();

        // when
        List<Dependency> additions = subject.plan(classpath);

        // then
        assertThat(additions)
                .singleElement()
                .satisfies(dependency -> assertTestDependency(
                        dependency,
                        TestDependencyPlanner.SPRING_TEST_PROFILER_GROUP_ID,
                        TestDependencyPlanner.SPRING_TEST_PROFILER_ARTIFACT_ID,
                        PROFILER_VERSION));
    }

    @Test
    void doesNotAddProfilerWhenAlreadyPresent() {
        // given
        ResolvedClasspath classpath = classpathOf(profiler("0.1.1"));

        // when
        List<Dependency> additions = subject.plan(classpath);

        // then
        assertThat(additions).isEmpty();
    }

    private static Artifact profiler(String version) {
        return artifact(
                TestDependencyPlanner.SPRING_TEST_PROFILER_GROUP_ID,
                TestDependencyPlanner.SPRING_TEST_PROFILER_ARTIFACT_ID,
                version);
    }

    private static Artifact artifact(String groupId, String artifactId, String version) {
        return new DefaultArtifact(
                groupId, artifactId, version, "test", "jar", null, new DefaultArtifactHandler("jar"));
    }

    private static ResolvedClasspath classpathOf(Artifact... artifacts) {
        Set<Artifact> set = new HashSet<>(Arrays.asList(artifacts));
        return new ResolvedClasspath(set);
    }

    private static void assertTestDependency(Dependency dependency, String groupId, String artifactId, String version) {
        assertThat(dependency.getGroupId()).isEqualTo(groupId);
        assertThat(dependency.getArtifactId()).isEqualTo(artifactId);
        assertThat(dependency.getVersion()).isEqualTo(version);
        assertThat(dependency.getScope()).isEqualTo(TestDependencyPlanner.TEST_SCOPE);
    }
}
