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

import com.axelixlabs.axelix.master.service.features.DefaultFeatureAvailabilityService;
import com.axelixlabs.axelix.master.service.features.FeatureAvailabilityService;
import com.axelixlabs.axelix.master.service.features.FeatureManifestLoader;
import com.axelixlabs.axelix.master.service.features.StarterFeaturesCatalog;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Configuration of the per-instance feature availability.
 *
 * @author Mikhail Polivakha
 */
@AutoConfiguration
public class FeaturesAutoConfiguration {

    @Bean
    public FeatureManifestLoader featureManifestLoader() {
        return new FeatureManifestLoader(new DefaultResourceLoader(), FeatureManifestLoader.DEFAULT_LOCATION);
    }

    @Bean
    public StarterFeaturesCatalog featureCatalog(FeatureManifestLoader featureManifestLoader) {
        return new StarterFeaturesCatalog(featureManifestLoader);
    }

    @Bean
    public FeatureAvailabilityService featureAvailabilityService(
            InstanceRegistry instanceRegistry, StarterFeaturesCatalog starterFeaturesCatalog) {
        return new DefaultFeatureAvailabilityService(instanceRegistry, starterFeaturesCatalog);
    }
}
