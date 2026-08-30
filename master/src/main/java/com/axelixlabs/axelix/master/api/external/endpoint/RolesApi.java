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

import java.util.List;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.response.RoleFeedResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.service.state.auth.RoleService;

/**
 * The API for working with roles.
 *
 * @author Sergey Cherkasov
 */
@Tag(name = "API for working with Roles", description = "The endpoints for listing the roles Axelix Master knows about")
@ExternalApiRestController
public class RolesApi {

    private final RoleService roleService;

    public RolesApi(RoleService roleService) {
        this.roleService = roleService;
    }

    @DefaultApiResponse(summary = "Retrieve all roles feed")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = RoleFeedResponse.class))))
    @GetMapping(path = ApiPaths.RolesApi.ROLES_FEED)
    public ResponseEntity<List<RoleFeedResponse>> getRolesFeed() {
        List<RoleFeedResponse> roles =
                roleService.getRolesFeed().stream().map(RoleFeedResponse::from).toList();

        return ResponseEntity.ok(roles);
    }
}
