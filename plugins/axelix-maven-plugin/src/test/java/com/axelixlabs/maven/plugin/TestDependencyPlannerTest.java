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
import java.util.stream.Collectors;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class TestDependencyPlannerTest {

    private static final String PROFILER_VERSION = "0.1.2";
    private static final String THYMELEAF_VERSION = "3.1.5";
    private static final String THYMELEAF_MIN_VERSION = "3.1.3";

    private final TestDependencyPlanner subject =
            new TestDependencyPlanner(PROFILER_VERSION, THYMELEAF_VERSION, THYMELEAF_MIN_VERSION);

    @Nested
    class SpringTestProfiler {

        @Test
        void addsProfilerWhenAbsent() {
            // given
            ResolvedClasspath classpath = classpathOf(thymeleaf("3.1.5"));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(additions)
                    .anySatisfy(dependency -> assertTestDependency(
                            dependency,
                            TestDependencyPlanner.SPRING_TEST_PROFILER_GROUP_ID,
                            TestDependencyPlanner.SPRING_TEST_PROFILER_ARTIFACT_ID,
                            PROFILER_VERSION));
        }

        @Test
        void doesNotAddProfilerWhenAlreadyPresent() {
            // given
            ResolvedClasspath classpath = classpathOf(profiler("0.1.1"), thymeleaf("3.1.5"));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(coordinatesOf(additions))
                    .doesNotContain(TestDependencyPlanner.SPRING_TEST_PROFILER_GROUP_ID + ":"
                            + TestDependencyPlanner.SPRING_TEST_PROFILER_ARTIFACT_ID);
        }
    }

    @Nested
    class Thymeleaf {

        @Test
        void addsThymeleafWhenAbsent() {
            // given
            ResolvedClasspath classpath = classpathOf(profiler("0.1.2"));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(additions)
                    .anySatisfy(dependency -> assertTestDependency(
                            dependency,
                            TestDependencyPlanner.THYMELEAF_GROUP_ID,
                            TestDependencyPlanner.THYMELEAF_ARTIFACT_ID,
                            THYMELEAF_VERSION));
        }

        @ParameterizedTest
        @ValueSource(strings = {"3.0.15", "3.1.0", "3.1.2"})
        void addsThymeleafWhenResolvedBelowMinimum(String resolvedVersion) {
            // given
            ResolvedClasspath classpath = classpathOf(profiler("0.1.2"), thymeleaf(resolvedVersion));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(additions)
                    .anySatisfy(dependency -> assertTestDependency(
                            dependency,
                            TestDependencyPlanner.THYMELEAF_GROUP_ID,
                            TestDependencyPlanner.THYMELEAF_ARTIFACT_ID,
                            THYMELEAF_VERSION));
        }

        @ParameterizedTest
        @ValueSource(strings = {"3.1.3", "3.1.5", "3.1.6", "3.2.0"})
        void doesNotAddThymeleafWhenResolvedAtOrAboveMinimum(String resolvedVersion) {
            // given
            ResolvedClasspath classpath = classpathOf(profiler("0.1.2"), thymeleaf(resolvedVersion));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(coordinatesOf(additions))
                    .doesNotContain(TestDependencyPlanner.THYMELEAF_GROUP_ID + ":"
                            + TestDependencyPlanner.THYMELEAF_ARTIFACT_ID);
        }
    }

    @Nested
    class Combined {

        @Test
        void addsBothWhenProfilerAbsentAndThymeleafOutdated() {
            // given
            ResolvedClasspath classpath = classpathOf(thymeleaf("3.0.15"));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(coordinatesOf(additions))
                    .containsExactlyInAnyOrder(
                            TestDependencyPlanner.SPRING_TEST_PROFILER_GROUP_ID + ":"
                                    + TestDependencyPlanner.SPRING_TEST_PROFILER_ARTIFACT_ID,
                            TestDependencyPlanner.THYMELEAF_GROUP_ID + ":"
                                    + TestDependencyPlanner.THYMELEAF_ARTIFACT_ID);
            assertThat(additions)
                    .allSatisfy(dependency ->
                            assertThat(dependency.getScope()).isEqualTo(TestDependencyPlanner.TEST_SCOPE));
        }

        @Test
        void addsNothingWhenBothPresentAndUpToDate() {
            // given
            ResolvedClasspath classpath = classpathOf(profiler("0.1.2"), thymeleaf("3.1.6"));

            // when
            List<Dependency> additions = subject.plan(classpath);

            // then
            assertThat(additions).isEmpty();
        }
    }

    private static Artifact profiler(String version) {
        return artifact(
                TestDependencyPlanner.SPRING_TEST_PROFILER_GROUP_ID,
                TestDependencyPlanner.SPRING_TEST_PROFILER_ARTIFACT_ID,
                version);
    }

    private static Artifact thymeleaf(String version) {
        return artifact(TestDependencyPlanner.THYMELEAF_GROUP_ID, TestDependencyPlanner.THYMELEAF_ARTIFACT_ID, version);
    }

    private static Artifact artifact(String groupId, String artifactId, String version) {
        return new DefaultArtifact(
                groupId, artifactId, version, "test", "jar", null, new DefaultArtifactHandler("jar"));
    }

    private static ResolvedClasspath classpathOf(Artifact... artifacts) {
        Set<Artifact> set = new HashSet<>(Arrays.asList(artifacts));
        return new ResolvedClasspath(set);
    }

    private static List<String> coordinatesOf(List<Dependency> dependencies) {
        return dependencies.stream()
                .map(dependency -> dependency.getGroupId() + ":" + dependency.getArtifactId())
                .collect(Collectors.toList());
    }

    private static void assertTestDependency(Dependency dependency, String groupId, String artifactId, String version) {
        assertThat(dependency.getGroupId()).isEqualTo(groupId);
        assertThat(dependency.getArtifactId()).isEqualTo(artifactId);
        assertThat(dependency.getVersion()).isEqualTo(version);
        assertThat(dependency.getScope()).isEqualTo(TestDependencyPlanner.TEST_SCOPE);
    }
}
