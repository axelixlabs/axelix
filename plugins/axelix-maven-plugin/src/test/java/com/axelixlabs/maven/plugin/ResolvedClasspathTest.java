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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ResolvedClasspathTest {

    @Test
    void returnsResolvedVersionWhenArtifactPresent() {
        // given
        Artifact thymeleaf = artifact("org.thymeleaf", "thymeleaf", "3.1.2");
        ResolvedClasspath subject = new ResolvedClasspath(Collections.singleton(thymeleaf));

        // when
        Optional<String> version = subject.versionOf("org.thymeleaf", "thymeleaf");

        // then
        assertThat(version).contains("3.1.2");
    }

    @Test
    void returnsEmptyWhenArtifactAbsent() {
        // given
        ResolvedClasspath subject = new ResolvedClasspath(Set.of(artifact("org.thymeleaf", "thymeleaf", "3.1.2")));

        // when
        Optional<String> version = subject.versionOf("digital.pragmatech.testing", "spring-test-profiler");

        // then
        assertThat(version).isEmpty();
    }

    private static Artifact artifact(String groupId, String artifactId, String version) {
        return new DefaultArtifact(
                groupId, artifactId, version, "test", "jar", null, new DefaultArtifactHandler("jar"));
    }
}
