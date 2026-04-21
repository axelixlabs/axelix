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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.UserCreateRequest;
import com.axelixlabs.axelix.master.api.external.request.UserDeleteRequest;
import com.axelixlabs.axelix.master.api.external.request.UserUpdateEmailRequest;
import com.axelixlabs.axelix.master.api.external.request.UserUpdatePasswordRequest;
import com.axelixlabs.axelix.master.api.external.request.UserUpdateRoleRequest;
import com.axelixlabs.axelix.master.api.external.request.UserUpdateUsernameRequest;
import com.axelixlabs.axelix.master.api.external.response.UserResponse;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;

/**
 * The API to manage users (view, create, delete, update).
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Users Management API",
        description = "The endpoints for viewing, creating, deleting managed users and modifying")
@ExternalApiRestController
public class UserManagementApi {

    /**
     * Provide a users feed containing sample UserResponse entries.
     *
     * @return a ResponseEntity with HTTP 200 and a list of UserResponse objects representing sample users
     */
    @DefaultApiResponse(summary = "Retrieve all users feed")
    @ApiResponse(
            description = "OK",
            responseCode = "200",
            content =
                    @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = UserResponse.class))))
    @GetMapping(path = ApiPaths.UsersManagementApi.USERS_VIEW)
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

    /**
     * Create a new user account from the provided request payload.
     *
     * @param request the payload containing the new user's details
     * @return HTTP 201 Created with an empty response body
     */
    @DefaultApiResponse(summary = "Create a new user")
    @ApiResponse(description = "Created", responseCode = "201")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_CREATE)
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * Deletes the user identified in the request.
     *
     * @param request the delete request containing the username of the user to remove
     * @return a 204 No Content response when the deletion request is accepted
     */
    @DefaultApiResponse(summary = "Delete a user by username")
    @ApiResponse(description = "No Content", responseCode = "204")
    @DeleteMapping(path = ApiPaths.UsersManagementApi.USERS_DELETE)
    public ResponseEntity<Void> deleteUser(@RequestBody UserDeleteRequest request) {

        return ResponseEntity.noContent().build();
    }

    /**
     * Replace a user's username.
     *
     * @param request the request containing the target user's identifier and the new username
     * @return a ResponseEntity with HTTP 204 No Content
     */
    @DefaultApiResponse(summary = "Replace the username of a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_UPDATE_USERNAME)
    public ResponseEntity<Void> updateUsername(@RequestBody UserUpdateUsernameRequest request) {

        return ResponseEntity.noContent().build();
    }

    /**
     * Replace a user's email address.
     *
     * @param request the request payload containing the user identifier and the new email address
     * @return `204 No Content` response indicating the update was accepted
     */
    @DefaultApiResponse(summary = "Replace the email of a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_UPDATE_EMAIL)
    public ResponseEntity<Void> updateEmail(@RequestBody UserUpdateEmailRequest request) {

        return ResponseEntity.noContent().build();
    }

    /**
     * Replaces a user's password.
     *
     * @param request the request containing the user's identifier and the new password
     * @return a ResponseEntity with HTTP 204 No Content when the password has been replaced
     */
    @DefaultApiResponse(summary = "Replace the password of a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_UPDATE_PASSWORD)
    public ResponseEntity<Void> updatePassword(@RequestBody UserUpdatePasswordRequest request) {

        return ResponseEntity.noContent().build();
    }

    /**
     * Replaces the role assigned to a user.
     *
     * @param request contains the identifier of the user and the new role to apply
     * @return an empty response with HTTP status 204 (No Content)
     */
    @DefaultApiResponse(summary = "Replace the role of a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_UPDATE_ROLE)
    public ResponseEntity<Void> updateRole(@RequestBody UserUpdateRoleRequest request) {

        return ResponseEntity.noContent().build();
    }
}
