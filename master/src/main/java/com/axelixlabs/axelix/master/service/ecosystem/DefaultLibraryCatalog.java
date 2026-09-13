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
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.ArtifactCoordinates;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProjectId;

/**
 * The catalog held fully in memory and indexed by coordinates. It is immutable once built, so it is safe to share
 * across requests.
 * <p>
 * The two uniqueness invariants of the curated data - one entry per id, one entry per artifact - are enforced here,
 * at construction, rather than at lookup. A catalog that violates them cannot be built at all, which turns a
 * curation mistake into a startup failure instead of a wrong verdict silently shown to a user.
 *
 * @author Mikhail Polivakha
 */
public class DefaultLibraryCatalog implements LibraryCatalog {

    private final Map<SoftwareProjectId, SoftwareProject> byId;
    private final Map<ArtifactCoordinates, SoftwareProject> byCoordinates;

    /**
     * @param libraries the curated entries, typically the merged result of every manifest
     *
     * @throws LibraryCatalogException when two entries share an id, or when two entries claim the same artifact
     */
    public DefaultLibraryCatalog(Collection<SoftwareProject> libraries) {
        this.byId = new LinkedHashMap<>(libraries.size());
        this.byCoordinates = new HashMap<>(libraries.size());

        for (SoftwareProject library : libraries) {
            index(library);
        }
    }

    private void index(SoftwareProject library) {
        SoftwareProject duplicateId = byId.putIfAbsent(library.id(), library);

        if (duplicateId != null) {
            throw new LibraryCatalogException("Duplicate library id '%s', declared by both '%s' and '%s'"
                    .formatted(library.id(), duplicateId.displayName(), library.displayName()));
        }

        for (ArtifactCoordinates coordinates : library.coordinates()) {
            SoftwareProject owner = byCoordinates.putIfAbsent(coordinates, library);

            if (owner != null) {
                throw new LibraryCatalogException("Artifact '%s' is claimed by both '%s' and '%s'"
                        .formatted(coordinates, owner.id(), library.id()));
            }
        }
    }

    @Override
    public Optional<SoftwareProject> find(ArtifactCoordinates coordinates) {
        return Optional.ofNullable(byCoordinates.get(coordinates));
    }

    @Override
    public Collection<SoftwareProject> all() {
        return Collections.unmodifiableCollection(byId.values());
    }
}
