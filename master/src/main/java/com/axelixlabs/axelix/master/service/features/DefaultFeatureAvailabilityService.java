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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.api.external.response.features.InstanceFeaturesResponse;
import com.axelixlabs.axelix.master.domain.Instance;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.exception.InstanceNotFoundException;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Default {@link FeatureAvailabilityService}.
 *
 * @author Mikhail Polivakha
 */
public class DefaultFeatureAvailabilityService implements FeatureAvailabilityService {

    private static final SemanticVersion FALLBACK_STARTER_VERSION = SemanticVersion.parse("1.1.0");

    private static final Logger log = LoggerFactory.getLogger(DefaultFeatureAvailabilityService.class);

    private final InstanceRegistry instanceRegistry;
    private final StarterFeaturesCatalog starterFeaturesCatalog;

    public DefaultFeatureAvailabilityService(
            InstanceRegistry instanceRegistry, StarterFeaturesCatalog starterFeaturesCatalog) {
        this.instanceRegistry = instanceRegistry;
        this.starterFeaturesCatalog = starterFeaturesCatalog;
    }

    @Override
    public InstanceFeaturesResponse resolveFeatures(InstanceId instanceId) {
        Instance instance =
                instanceRegistry.get(instanceId).orElseThrow(() -> new InstanceNotFoundException(instanceId));

        // Axelix Master rejects starters whose version cannot be parsed, so an unparseable version on a known instance
        // is anomalous.
        //
        // Still, we fall back rather than hard failing, so at worst a version we failed to read (which again, should
        // NOT happen) is treated as the oldest supported starter.
        SemanticVersion starterVersion = SemanticVersion.tryParse(instance.starterVersion())
                .orElseGet(() -> {
                    log.warn(
                            "Something goes wrong. Axelix Master accepted the version it cannot parse: {}. Report back to maintainers. That is a bug.",
                            instance.serviceVersion());
                    return FALLBACK_STARTER_VERSION;
                });

        return new InstanceFeaturesResponse(
                instanceId.instanceId(), starterFeaturesCatalog.availableFor(starterVersion));
    }
}
