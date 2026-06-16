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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.maven.artifact.Artifact;

/**
 * A read-only view over the resolved (transitive) dependency artifacts of a Maven project that
 * answers questions about presence and resolved version of a given {@code groupId:artifactId}.
 */
final class ResolvedClasspath {

    private final Set<Artifact> artifacts;

    ResolvedClasspath(Set<Artifact> artifacts) {
        this.artifacts = Objects.requireNonNull(artifacts, "artifacts");
    }

    /**
     * Returns the resolved version of the first artifact matching {@code groupId:artifactId}, or an
     * empty optional when no such artifact is present on the classpath.
     */
    Optional<String> versionOf(String groupId, String artifactId) {
        return artifacts.stream()
                .filter(artifact ->
                        groupId.equals(artifact.getGroupId()) && artifactId.equals(artifact.getArtifactId()))
                .map(Artifact::getVersion)
                .findFirst();
    }
}
