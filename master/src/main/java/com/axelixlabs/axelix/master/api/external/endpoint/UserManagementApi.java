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

import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.axelixlabs.axelix.master.api.external.ApiPaths;
import com.axelixlabs.axelix.master.api.external.ExternalApiRestController;
import com.axelixlabs.axelix.master.api.external.request.user.UserCreateRequest;
import com.axelixlabs.axelix.master.api.external.request.user.UserDeleteRequest;
import com.axelixlabs.axelix.master.api.external.request.user.UserUpdateRequest;
import com.axelixlabs.axelix.master.api.external.swagger.DefaultApiResponse;
import com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration;
import com.axelixlabs.axelix.master.exception.auth.UserInvalidValueException;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.service.state.UserService;

/**
 * The API to manage users (view, create, delete, update).
 *
 * @author Sergey Cherkasov
 */
@Tag(
        name = "Users Management API",
        description = "The endpoints for viewing, creating, deleting, and modifying managed users")
@ExternalApiRestController
@ConditionalOnProperty(
        prefix = SecurityAutoConfiguration.LOCAL_LOGIN_PROPERTIES_PREFIX,
        name = "enabled",
        havingValue = "true")
public class UserManagementApi {

    private final UserService userService;

    public UserManagementApi(UserService userService) {
        this.userService = userService;
    }

    @DefaultApiResponse(summary = "Create a new user")
    @ApiResponse(description = "Created", responseCode = "201")
    @PostMapping(path = ApiPaths.UsersManagementApi.USERS_CREATE)
    public ResponseEntity<Void> createUser(@RequestBody UserCreateRequest request) {
        try {

            userService.createLocal(request.username(), request.email(), request.password(), request.role());
            return ResponseEntity.status(HttpStatus.CREATED).build();

        } catch (UserInvalidValueException | UserRoleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DefaultApiResponse(summary = "Delete a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @DeleteMapping(path = ApiPaths.UsersManagementApi.USERS_DELETE)
    public ResponseEntity<Void> deleteUser(@RequestBody UserDeleteRequest request) {

        userService.deleteById(request.id());
        return ResponseEntity.noContent().build();
    }

    @DefaultApiResponse(summary = "Update a user")
    @ApiResponse(description = "No Content", responseCode = "204")
    @PutMapping(path = ApiPaths.UsersManagementApi.USERS_UPDATE)
    public ResponseEntity<Void> updateUser(@RequestBody UserUpdateRequest request) {
        try {
            userService.updateUserPatch(
                    request.id(), request.username(), request.email(), request.password(), request.roles());

            return ResponseEntity.noContent().build();

        } catch (UserInvalidValueException | UserRoleNotFoundException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
