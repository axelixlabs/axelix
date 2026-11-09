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
package com.axelixlabs.axelix.master.api.external.endpoint.rbac;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.AuthorityProfileResponse;
import com.axelixlabs.axelix.master.service.auth.AuthoritiesService;

/**
 * The API for reading the authority catalog.
 *
 * @author Mikhail Polivakha
 */
@ExternalApiRestController
public class AuthoritiesApi {

    private final AuthoritiesService authoritiesService;

    public AuthoritiesApi(AuthoritiesService authoritiesService) {
        this.authoritiesService = authoritiesService;
    }

    @GetMapping(path = ApiPaths.AuthoritiesApi.AUTHORITIES_FEED)
    public ResponseEntity<List<AuthorityProfileResponse>> getAuthoritiesFeed() {
        List<AuthorityProfileResponse> authorities = authoritiesService.getAuthoritiesFeed().stream()
                .map(AuthorityProfileResponse::from)
                .toList();

        return ResponseEntity.ok(authorities);
    }
}
