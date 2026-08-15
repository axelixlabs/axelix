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
import java.util.Set;

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.master.domain.UserEntity;

/**
 * Public view of a managed user.
 *
 * @param id          Unique identifier of the user.
 * @param username    Login name of the user.
 * @param firstName   First name of the user, which may be {@code null}.
 * @param lastName    Last name of the user, which may be {@code null}.
 * @param email       Email address of the user, which may be {@code null}.
 * @param jobTitle    Job title of the user, which may be {@code null}.
 * @param organizationalUnit Organizational unit of the user, which may be {@code null}.
 * @param roles       The names of roles granted to this user.
 * @param userOrigin  Origin of the user account.
 * @param status      Status that controls whether the user can log in.
 * @param lastLoginAt Timestamp of the most recent successful login. {@code null} if the user has never logged in.
 *
 * @author Sergey Cherkasov
 * @author Mikhail Polivakha
 */
public record UserResponse(
        String id,
        String username,
        @Nullable String firstName,
        @Nullable String lastName,
        @Nullable String email,
        @Nullable String jobTitle,
        @Nullable String organizationalUnit,
        Set<String> roles,
        String userOrigin,
        String status,
        @Nullable Instant lastLoginAt) {

    /**
     * @param user      The user to expose.
     * @param roleNames Names of the roles granted to the user, read from {@code users_roles} rather than from the
     *                  legacy {@code users.roles} column.
     */
    public static UserResponse from(UserEntity user, Set<String> roleNames) {
        return new UserResponse(
                user.id(),
                user.username(),
                user.firstName(),
                user.lastName(),
                user.email(),
                user.jobTitle(),
                user.organizationalUnit(),
                roleNames,
                user.userOrigin().getDisplayName(),
                user.status().name(),
                user.lastLoginAt());
    }
}
