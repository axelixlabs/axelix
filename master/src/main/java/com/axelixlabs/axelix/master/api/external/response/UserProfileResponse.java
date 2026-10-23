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

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.service.state.auth.RoleService.GrantedRole;

/**
 * A single user as the profile page presents them
 *
 * @param id                 Unique identifier of the user.
 * @param username           Login username of the user.
 * @param firstName          First name of the user, {@code null} if not provided.
 * @param lastName           Last name of the user, {@code null} if not provided.
 * @param email              Email address of the user, {@code null} if not provided.
 * @param jobTitle           Job title of the user, {@code null} if not provided.
 * @param organizationalUnit Organizational unit of the user, {@code null} if not provided.
 * @param roles              Roles granted to the user, ordered by name.
 * @param authorities        Every authority the user holds, accumulated across all of their roles, ordered by name.
 * @param userOrigin         Display name of the origin the user account comes from.
 * @param status             Current status of the user.
 * @param lastLoginAt        Timestamp of the most recent successful login. {@code null} if the user has never logged in.
 */
public record UserProfileResponse(
        String id,
        String username,
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String email,
        @Nullable String jobTitle,
        @Nullable String organizationalUnit,
        List<RoleProfile> roles,
        Set<String> authorities,
        String userOrigin,
        String status,
        @Nullable Instant lastLoginAt) {

    /**
     * A role granted to the user.
     *
     * @param name        The unique name of the role.
     * @param description What the role is for, as stored alongside the role.
     * @param grantedAt   When this role was granted to the user.
     */
    public record RoleProfile(
            String name, String description, @Nullable Instant grantedAt) {}

    public static UserProfileResponse from(UserEntity user, List<GrantedRole> grantedRoles) {
        List<RoleProfile> roles = grantedRoles.stream()
                // The grant timestamp is not persisted anywhere yet, so the field is reserved rather than filled.
                .map(role -> new RoleProfile(role.name(), role.description(), null))
                .toList();

        Set<String> authorities = grantedRoles.stream()
                .flatMap(role -> role.authorities().stream())
                .collect(Collectors.toCollection(TreeSet::new));

        return new UserProfileResponse(
                user.id(),
                user.username(),
                user.firstName(),
                user.lastName(),
                user.email(),
                user.jobTitle(),
                user.organizationalUnit(),
                roles,
                authorities,
                user.userOrigin().getDisplayName(),
                user.status().name(),
                user.lastLoginAt());
    }
}
