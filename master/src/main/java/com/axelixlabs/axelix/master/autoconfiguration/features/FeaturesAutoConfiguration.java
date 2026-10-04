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
package com.axelixlabs.axelix.master.autoconfiguration.features;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.DefaultResourceLoader;

import com.axelixlabs.axelix.master.service.features.DefaultInstanceFeatureAvailabilityService;
import com.axelixlabs.axelix.master.service.features.InstanceFeatureAvailabilityService;
import com.axelixlabs.axelix.master.service.features.InstanceFeaturesCatalog;
import com.axelixlabs.axelix.master.service.features.InstanceFeaturesManifestLoader;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Configuration of the per-instance feature availability.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class FeaturesAutoConfiguration {

    @Bean
    public InstanceFeaturesManifestLoader featureManifestLoader() {
        return new InstanceFeaturesManifestLoader(
                new DefaultResourceLoader(), InstanceFeaturesManifestLoader.DEFAULT_LOCATION);
    }

    @Bean
    public InstanceFeaturesCatalog featureCatalog(InstanceFeaturesManifestLoader instanceFeaturesManifestLoader) {
        return new InstanceFeaturesCatalog(instanceFeaturesManifestLoader);
    }

    @Bean
    public InstanceFeatureAvailabilityService featureAvailabilityService(
            InstanceRegistry instanceRegistry, InstanceFeaturesCatalog instanceFeaturesCatalog) {
        return new DefaultInstanceFeatureAvailabilityService(instanceRegistry, instanceFeaturesCatalog);
    }
}
