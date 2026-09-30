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
package com.axelixlabs.axelix.master.api.external.endpoint;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.features.InstanceFeaturesResponse;
import com.axelixlabs.axelix.master.domain.InstanceId;
import com.axelixlabs.axelix.master.service.features.InstanceFeatureAvailabilityService;

/**
 * The API for resolving which features are available for a managed instance, that Axelix Master UI would care about.
 * <p>
 * The front-end needs it so it can render only the features the instance's supports.
 *
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class InstanceFeaturesApi {

    private final InstanceFeatureAvailabilityService instanceFeatureAvailabilityService;

    public InstanceFeaturesApi(InstanceFeatureAvailabilityService instanceFeatureAvailabilityService) {
        this.instanceFeatureAvailabilityService = instanceFeatureAvailabilityService;
    }

    @GetMapping(path = ApiPaths.InstanceFeaturesApi.INSTANCE_ID)
    public InstanceFeaturesResponse getFeatures(@PathVariable("instanceId") String instanceId) {
        return instanceFeatureAvailabilityService.resolveFeatures(InstanceId.of(instanceId));
    }
}
