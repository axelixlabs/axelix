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
package com.axelixlabs.axelix.master.service.ecosystem.platform;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.axelixlabs.axelix.master.domain.ecosystem.platform.Platform;
import com.axelixlabs.axelix.master.domain.ecosystem.platform.PlatformName;

/**
 * The immutable, in-memory {@link PlatformCatalog}, built once at startup from the curated manifests.
 *
 * @author Mikhail Polivakha
 */
public class DefaultPlatformCatalog implements PlatformCatalog {

    private final Map<PlatformName, Platform> cache;

    /**
     * @throws PlatformCatalogException if anything goes wrong during parsing a platform YAML catalog
     */
    public DefaultPlatformCatalog(PlatformManifestLoader platformManifestLoader) throws PlatformCatalogException {

        List<Platform> load = platformManifestLoader.load();
        Map<PlatformName, Platform> cache = new HashMap<>();

        for (Platform policy : load) {
            Platform previous = cache.putIfAbsent(policy.name(), policy);

            if (previous != null) {
                throw new PlatformCatalogException(
                        "Platform '%s' is declared by two manifests".formatted(policy.name()));
            }
        }

        this.cache = Map.copyOf(cache);
    }

    @Override
    public Optional<Platform> find(PlatformName platformName) {
        return Optional.ofNullable(cache.get(platformName));
    }
}
