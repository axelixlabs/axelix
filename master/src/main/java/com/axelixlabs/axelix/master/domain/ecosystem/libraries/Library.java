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
package com.axelixlabs.axelix.master.domain.ecosystem.libraries;

import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;

/**
 * The version-free library of a single published artifact, i.e. the G and the A inside the GAV coordinate. This
 * is the key a resolved dependency of a managed application is looked up by in the {@link SoftwareProject} catalog.
 * <p>
 * The version is deliberately absent. Support status is a property of the project as a whole rather than of the
 * version an application happens to resolve, and in the Java ecosystem a change of the maintained line almost always
 * comes with a change of libraries ({@code net.sf.ehcache:ehcache} versus {@code org.ehcache:ehcache},
 * {@code commons-lang:commons-lang} versus {@code org.apache.commons:commons-lang3}), so the two lines are already
 * distinct keys here.
 *
 * @param groupId    the group id of the artifact (the G inside the GAV coordinate)
 * @param artifactId the artifact id of the artifact (the A inside the GAV coordinate)
 *
 * @author Mikhail Polivakha
 */
public record Library(String groupId, String artifactId) {

    private static final String SEPARATOR = ":";

    public static Library of(String groupId, String artifactId) {
        return new Library(groupId, artifactId);
    }

    /**
     * Parses the canonical {@code groupId:artifactId} notation, which is how libraries are written in the curated
     * catalog manifests.
     *
     * @param coordinates the libraries in the {@code groupId:artifactId} notation
     *
     * @return the parsed libraries
     *
     * @throws IllegalArgumentException when the given value is not exactly two non-blank, colon-separated segments,
     *                                  in particular when a version was mistakenly included
     */
    public static Library parse(String coordinates) {
        String[] segments = coordinates.split(SEPARATOR, -1);

        if (segments.length != 2 || segments[0].isBlank() || segments[1].isBlank()) {
            throw new IllegalArgumentException(
                    "Expected libraries in the 'groupId:artifactId' notation, but got '%s'".formatted(coordinates));
        }

        return new Library(segments[0].trim(), segments[1].trim());
    }

    @Override
    public String toString() {
        return groupId + SEPARATOR + artifactId;
    }
}
