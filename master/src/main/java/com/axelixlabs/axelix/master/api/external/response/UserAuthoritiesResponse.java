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
package com.axelixlabs.axelix.master.api.external.response;

import java.util.List;

import com.axelixlabs.axelix.master.repository.AuthorityRepository;
import com.axelixlabs.axelix.master.service.auth.UserAuthoritiesService.UserAuthorities;

/**
 * The authority catalog partitioned by a user's effective role grants.
 */
public record UserAuthoritiesResponse(
        List<AuthorityProfileResponse> effectiveAuthorities, List<AuthorityProfileResponse> ungrantedAuthorities) {

    public static UserAuthoritiesResponse from(UserAuthorities authorities) {
        return new UserAuthoritiesResponse(
                authorities.effectiveAuthorities().stream()
                        .map(AuthorityProfileResponse::from)
                        .toList(),
                authorities.ungrantedAuthorities().stream()
                        .map(AuthorityProfileResponse::from)
                        .toList());
    }

    /**
     * Public view of an authority, including its identifier, unique codename, display name and description.
     */
    public record AuthorityProfileResponse(String id, String codename, String displayName, String description) {

        public static AuthorityProfileResponse from(AuthorityRepository.AuthorityProfile authority) {
            return new AuthorityProfileResponse(
                    authority.id(), authority.codename(), authority.displayName(), authority.description());
        }
    }
}
