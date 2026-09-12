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
package com.axelixlabs.axelix.master.domain.dependencies;

import java.util.Set;

import org.jspecify.annotations.Nullable;

/**
 * One curated entry of the library catalog: everything Axelix claims about a single upstream project, and the
 * evidence for it.
 * <p>
 * The unit of curation is the <em>project</em>, not the artifact. Jackson publishes {@code jackson-core},
 * {@code jackson-databind} and {@code jackson-annotations}, but there is one Jackson and one support story, so one
 * entry owns all three sets of {@link #coordinates()}. Curating per artifact would duplicate the prose across every
 * artifact of the same project and let the copies drift apart.
 * <p>
 * An entry is only worth writing when Axelix has something to say. A project absent from the catalog is not a claim
 * that it is unhealthy - it is the absence of a claim, and the UI renders it as a plain row.
 *
 * @param id          the stable identifier of the project
 * @param displayName the name the project is known by, e.g. {@code Spring Cloud Sleuth}
 * @param ecosystem   the area of a running application the project belongs to
 * @param status      what the authors are still doing with the project
 * @param summary     what happened to the project, in prose, as shown in the UI
 * @param coordinates every artifact the project publishes that a managed application might resolve; never empty
 * @param succession  where to go instead, or null when the project is {@link SupportStatus#ACTIVE}
 * @param reference   the upstream page backing the {@code status}
 *
 * @author Mikhail Polivakha
 */
public record KnownLibrary(
        LibraryId id,
        String displayName,
        Ecosystem ecosystem,
        SupportStatus status,
        String summary,
        Set<ArtifactCoordinates> coordinates,
        @Nullable Succession succession,
        Reference reference) {

    public KnownLibrary {
        if (coordinates.isEmpty()) {
            throw new IllegalArgumentException(
                    "Library '%s' declares no coordinates, so nothing could ever match it".formatted(id));
        }

        coordinates = Set.copyOf(coordinates);
    }

    /**
     * Whether the project is something a team should look at. This is the predicate the whole UI hangs off: a flagged
     * library gets a chip and an accent, everything else renders plainly.
     *
     * @return true when the project is no longer actively developed
     */
    public boolean isFlagged() {
        return status != SupportStatus.ACTIVE;
    }
}
