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
package com.axelixlabs.axelix.master.service.dependencies;

import java.util.Collection;
import java.util.Optional;

import com.axelixlabs.axelix.master.domain.dependencies.ArtifactCoordinates;
import com.axelixlabs.axelix.master.domain.dependencies.KnownLibrary;

/**
 * The read side of the curated catalog of known Java libraries. Given the coordinates of a dependency a managed
 * application resolved at runtime, it answers what Axelix knows about the project behind it.
 *
 * @author Mikhail Polivakha
 */
public interface LibraryCatalog {

    /**
     * Looks up the project that publishes the given artifact.
     *
     * @param coordinates the version-free coordinates of a resolved dependency
     *
     * @return the curated entry, or {@link Optional#empty()} when Axelix has nothing to say about this artifact.
     *         An empty result is not a negative verdict - it only means the artifact is not curated.
     */
    Optional<KnownLibrary> find(ArtifactCoordinates coordinates);

    /**
     * Every curated entry, in the order the manifests declare them. Intended for administrative views and for the
     * tests that assert the catalog as a whole, not for per-request lookups.
     *
     * @return an unmodifiable view of the whole catalog
     */
    Collection<KnownLibrary> all();
}
