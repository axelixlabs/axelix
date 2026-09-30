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
package com.axelixlabs.axelix.master.service.features;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.domain.features.Feature;

/**
 * The catalog of the {@link Feature Features} Axelix Master is aware about, built from the curated manifest at startup.
 * The catalog resolves, for a given starter version, the set of features a service running that version exposes.
 *
 * @author Mikhail Polivakha
 */
public class StarterFeaturesCatalog {

    private final List<Feature> starterFeatures;

    /**
     * @throws FeatureCatalogException when the manifest cannot be loaded, or declares a feature id more than once.
     */
    public StarterFeaturesCatalog(FeatureManifestLoader loader) {
        this.starterFeatures = loader.load();
        assertUniqueIds();
    }

    private void assertUniqueIds() {
        Set<String> seen = new HashSet<>(starterFeatures.size());

        for (Feature feature : starterFeatures) {
            if (!seen.add(feature.id())) {
                throw new FeatureCatalogException("Feature id '%s' is declared more than once".formatted(feature.id()));
            }
        }
    }

    /**
     * Resolves the features available for a service running the given starter version.
     *
     * @param starterVersion the starter version of a managed instance.
     * @return the ids of the features available for that instance, in manifest order.
     */
    public List<String> availableFor(SemanticVersion starterVersion) {
        return starterFeatures.stream()
                .filter(feature -> feature.isAvailableFor(starterVersion))
                .map(Feature::id)
                .toList();
    }
}
