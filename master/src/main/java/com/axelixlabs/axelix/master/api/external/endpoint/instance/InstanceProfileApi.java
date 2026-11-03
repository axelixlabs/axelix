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
package com.axelixlabs.axelix.master.api.external.endpoint.instance;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.profile.InstanceProfileResponse;
import com.axelixlabs.axelix.master.domain.instance.InstanceId;
import com.axelixlabs.axelix.master.service.profile.InstanceProfileService;

/**
 * The API for the profile of a managed instance, which the front-end renders the instance view around: the
 * instance itself, its siblings and the features it has.
 *
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class InstanceProfileApi {

    private final InstanceProfileService instanceProfileService;

    public InstanceProfileApi(InstanceProfileService instanceProfileService) {
        this.instanceProfileService = instanceProfileService;
    }

    @GetMapping(path = ApiPaths.InstanceProfileApi.PROFILE)
    public InstanceProfileResponse getProfile(@PathVariable("instanceId") String instanceId) {
        return instanceProfileService.getProfile(InstanceId.of(instanceId));
    }
}
