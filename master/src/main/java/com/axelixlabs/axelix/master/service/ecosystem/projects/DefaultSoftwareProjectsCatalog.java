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
package com.axelixlabs.axelix.master.service.ecosystem.projects;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.axelixlabs.axelix.master.domain.ecosystem.libraries.Library;
import com.axelixlabs.axelix.master.domain.ecosystem.projects.SoftwareProject;

/**
 * Default {@link SoftwareProjectsCatalog}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultSoftwareProjectsCatalog implements SoftwareProjectsCatalog {

    private final Map<Library, SoftwareProject> cache;

    /**
     * @throws SoftwareProjectsCatalogException in case of an error of loading the SoftwareProjects map.
     */
    public DefaultSoftwareProjectsCatalog(SoftwareProjectsManifestLoader softwareProjectsManifestLoader) {
        List<SoftwareProject> load = softwareProjectsManifestLoader.load();

        this.cache = new HashMap<>(load.size());

        for (SoftwareProject library : load) {
            index(library);
        }
    }

    private void index(SoftwareProject project) {
        for (Library library : project.libraries()) {
            SoftwareProject owner = cache.putIfAbsent(library, project);

            if (owner != null) {
                throw new SoftwareProjectsCatalogException(
                        "Artifact '%s' is claimed by both '%s' and '%s'".formatted(library, owner.id(), project.id()));
            }
        }
    }

    @Override
    public Optional<SoftwareProject> resolve(Library library) {
        return Optional.ofNullable(cache.get(library));
    }
}
