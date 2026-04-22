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

import java.time.Instant;
import java.util.List;
import java.util.Set;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.user.UserCreateRequest;
import com.axelixlabs.axelix.master.api.external.request.user.UserDeleteRequest;
import com.axelixlabs.axelix.master.api.external.request.user.UserUpdateRequest;
import com.axelixlabs.axelix.master.api.external.response.UserResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;

/**
 * The API to manage users (view, create, delete, update).
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Users Management API",
        description = "The endpoints for viewing, creating, deleting, and modifying managed users")
@ExternalApiRestController
public class UserManagementApi {

    @DefaultApiResponse(summary = "Retrieve all users feed")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @GetMapping(path = ApiPaths.UsersManagementApi.USERS_FEED)
    public ResponseEntity<List<UserResponse>> getUsersFeed() {
        Instant lastLoginAt = Instant.now();
        List<UserResponse> users = List.of(
                new UserResponse("id-0", "alice", "alice@gmail.com", Set.of("ADMIN"), "OIDC/OAuth2", lastLoginAt),
                new UserResponse("id-1", "bob", "bob@gmail.com", Set.of("EDITOR"), "Static", lastLoginAt),
                new UserResponse("id-2", "carol", "carol@gmail.com", Set.of("VIEWER"), "OIDC/OAuth2", lastLoginAt),
                new UserResponse("id-3", "dave", "dave@gmail.com", Set.of("ADMIN", "EDITOR"), "Static", lastLoginAt),
                new UserResponse("id-4", "eve", "eve@gmail.com", Set.of("VIEWER"), "OIDC/OAuth2", lastLoginAt));

        return ResponseEntity.ok(users);
    }

    @DefaultApiResponse(summary = "Create a new user")
    @ApiResponse(description = "Created", responseCode = "201")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_CREATE)
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DefaultApiResponse(summary = "Delete a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @DeleteMapping(path = ApiPaths.UsersManagementApi.USERS_DELETE)
    public ResponseEntity<Void> deleteUser(@RequestBody UserDeleteRequest request) {

        return ResponseEntity.noContent().build();
    }

    @DefaultApiResponse(summary = "Update a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PatchMapping(path = ApiPaths.UsersManagementApi.USERS_UPDATE)
    public ResponseEntity<Void> updateUser(@RequestBody UserUpdateRequest request) {

        return ResponseEntity.noContent().build();
    }
}
