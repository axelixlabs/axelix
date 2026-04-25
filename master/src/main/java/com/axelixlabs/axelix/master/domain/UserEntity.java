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

import com.axelixlabs.axelix.master.service.auth.Provider;

/**
 * Persistent user record for users created via the Users Management UI by a SUPER_ADMIN.
 *
 * @param id           Unique identifier of the user (UUID generated in the service). Serves as the primary key.
 * @param username     Login name of the user.
 * @param email        Email address of the user, which may be {@code null}.
 * @param password     Hash of the user's password, which may be {@code null}.
 * @param roles        Names of the roles granted to this user (e.g. {@code ADMIN}, {@code EDITOR}, {@code VIEWER}).
 * @param provider     Origin of the user account (e.g. {@code OIDC/OAUTH2} provider username, or {@code LOCAL}).
 * @param lastLoginAt  Timestamp of the most recent successful login. {@code null} until the user logs in for the first time.
 *
 * @author Sergey Cherkasov
 */
@Table("users")
public record UserEntity(
        @Id String id,
        String username,
        @Nullable String email,
        @Nullable String password,
        Roles roles,
        Provider provider,
        @Nullable Instant lastLoginAt) {

    public UserEntity withLastLoginAt(Instant lastLoginAt) {
        return new UserEntity(
                this.id, this.username, this.email, this.password, this.roles, this.provider, lastLoginAt);
    }

    public record Roles(Set<String> values) {
        public Roles {
            values = Set.copyOf(values);
        }
    }

    @Override
    public String toString() {
        return "User[id=" + id + ", username=" + username + ", email=" + email
                + ", password=[REDACTED], roles=" + roles + ", provider=" + provider
                + ", lastLoginAt=" + lastLoginAt + ']';
    }
}
