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
import com.axelixlabs.axelix.master.service.features.FeatureAvailabilityService;

/**
 * The API for resolving which Axelix UI features are available for a managed instance. The front-end queries it when
 * it enters the instance view, so it can render only the features the instance's starter version supports.
 *
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class FeaturesApi {

    private final FeatureAvailabilityService featureAvailabilityService;

    public FeaturesApi(FeatureAvailabilityService featureAvailabilityService) {
        this.featureAvailabilityService = featureAvailabilityService;
    }

    @GetMapping(path = ApiPaths.FeaturesApi.INSTANCE_ID)
    public InstanceFeaturesResponse getFeatures(@PathVariable("instanceId") String instanceId) {
        return featureAvailabilityService.resolveFeatures(InstanceId.of(instanceId));
    }
}
