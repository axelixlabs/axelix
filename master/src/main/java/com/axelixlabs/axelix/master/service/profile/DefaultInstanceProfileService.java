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
package com.axelixlabs.axelix.master.service.profile;

import java.util.EnumSet;
import java.util.Set;

import org.jspecify.annotations.NonNull;

import org.springframework.stereotype.Service;

import com.axelixlabs.axelix.common.contract.metadata.InstanceFeature;
import com.axelixlabs.axelix.common.utils.SemanticVersion;
import com.axelixlabs.axelix.master.api.external.response.profile.InstanceProfileResponse;
import com.axelixlabs.axelix.master.domain.instance.DetectedFeatures;
import com.axelixlabs.axelix.master.domain.instance.Instance;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.convert.response.InstancesToShortProfileConverter;
import com.axelixlabs.axelix.master.service.state.InstanceRegistry;

/**
 * Default {@link InstanceProfileService}.
 *
 * @author Mikhail Polivakha
 */
@Service
public class DefaultInstanceProfileService implements InstanceProfileService {

    private static final SemanticVersion DEPENDENCY_ANALYSIS_SINCE = SemanticVersion.parse("1.2.0");

    private final InstanceRegistry instanceRegistry;
    private final InstancesToShortProfileConverter instancesToShortProfileConverter;

    public DefaultInstanceProfileService(
            InstanceRegistry instanceRegistry, InstancesToShortProfileConverter instancesToShortProfileConverter) {
        this.instanceRegistry = instanceRegistry;
        this.instancesToShortProfileConverter = instancesToShortProfileConverter;
    }

    @Override
    public InstanceProfileResponse getProfile(InstanceId instanceId) {
        Instance instance = instanceRegistry.getOrThrow(instanceId);

        // The siblings always include the instance itself
        int siblingsCount = instanceRegistry.findSiblings(instanceId).size() - 1;

        return new InstanceProfileResponse(
                instancesToShortProfileConverter.convertNonNull(instance),
                siblingsCount,
                resolveFeatures(instance).stream()
                        .map(InstanceFeature::getValue)
                        .toList());
    }

    private static Set<InstanceFeature> resolveFeatures(Instance instance) {
        DetectedFeatures detectedFeatures = instance.detectedFeatures();

        if (detectedFeatures != null) {
            return detectedFeatures.features();
        }

        return inferAvailableFeatures(instance);
    }

    // in 1.6, once every instance in the compatibility window reports the features in use,
    // we should remove that method and rely solely on the state reported by starters (i.e. the DetectedFeatures).
    @Deprecated(forRemoval = true, since = "1.3")
    private static @NonNull Set<InstanceFeature> inferAvailableFeatures(Instance instance) {
        // Starters older than 1.3 do not detect their features. Since we cannot tell whether such an instance
        // has, say, caches, we assume that it has all of them - at worst, the corresponding page turns out empty.
        Set<InstanceFeature> assumed = EnumSet.of(
                InstanceFeature.CACHES,
                InstanceFeature.SCHEDULED_TASKS,
                InstanceFeature.ENTITIES_MAP,
                InstanceFeature.TRANSACTIONS);

        // The only feature older starters may lack entirely, since its endpoint appeared in 1.2
        boolean hasDependencyAnalysis = SemanticVersion.tryParse(instance.starterVersion())
                .map(version -> version.isAtLeast(DEPENDENCY_ANALYSIS_SINCE))
                .orElse(false);

        if (hasDependencyAnalysis) {
            assumed.add(InstanceFeature.DEPENDENCY_ANALYSIS);
        }

        return assumed;
    }
}
