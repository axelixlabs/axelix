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
package com.axelixlabs.axelix.sbs.spring.profiles;

import org.springframework.boot.actuate.endpoint.web.annotation.RestControllerEndpoint;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Custom Spring Boot Actuator endpoint
 * that exposes an operation for replacing active application profiles at runtime.
 *
 * @since 11.07.2025
 * @author Nikita Kirillov
 */
@RestControllerEndpoint(id = "axelix-profile-management")
public class ProfileManagementEndpoint {

    private final ProfileMutator profileMutator;

    public ProfileManagementEndpoint(ProfileMutator profileMutator) {
        this.profileMutator = profileMutator;
    }

    @PostMapping
    public ProfileMutationResponse replaceProfiles(@RequestBody ProfileMutationRequest request) {
        String[] effectiveProfiles = (request == null || request.effectiveProfiles() == null)
                ? new String[0]
                : request.effectiveProfiles().toArray(new String[0]);

        return profileMutator.replaceActiveProfiles(effectiveProfiles);
    }
}
