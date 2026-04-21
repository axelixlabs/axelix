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
package com.axelixlabs.axelix.master.domain;

import java.time.Instant;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Persistent user record for users created via the Users Management UI by a SUPER_ADMIN.
 *
 * @param id           Unique identifier of the user (UUID generated in the service). Serves as the primary key.
 * @param username     Login name of the user.
 * @param email        Email address of the user.
 * @param password     Hash of the user's password.
 * @param roles        Names of the roles granted to this user (e.g. {@code ADMIN}, {@code EDITOR}, {@code VIEWER}).
 * @param provider     Origin of the user account (e.g. OIDC/OAuth2 provider name, or {@code LOCAL} for login/password).
 * @param createdAt    Timestamp when the user was created.
 * @param lastLoginAt  Timestamp of the most recent successful login. {@code null} until the user logs in for the first time.
 *
 * @author Sergey Cherkasov
 */
@Table("users")
public record Users(
        @Id String id,
        String username,
        String email,
        String password,
        Set<String> roles,
        String provider,
        Instant createdAt,
        @Nullable Instant lastLoginAt) {

    public Users withLastLoginAt(Instant lastLoginAt) {
        return new Users(
                this.id,
                this.username,
                this.email,
                this.password,
                this.roles,
                this.provider,
                this.createdAt,
                lastLoginAt);
    }
}
