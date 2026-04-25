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
package com.axelixlabs.axelix.master.service.auth.provider;

import java.util.Set;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.UserRoleNotFoundException;
import com.axelixlabs.axelix.master.service.state.UserService;

/**
 * {@link UserProvider} that authenticates a given user against the users stored in the database.
 *
 * @author Sergey Cherkasov
 */
public class DatabaseUserProvider implements UserProvider {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DatabaseUserProvider(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public @Nullable User load(String username, String password) {

        UserEntity user = userService.getUserByUsername(username).orElse(null);

        if (user != null && user.password() != null && passwordEncoder.matches(password, user.password())) {
            userService.updateLastLoginAt(user.username());
            return new DefaultUser(user.username(), user.password(), extractRoles(user.roles()));
        }

        return null;
    }

    private Set<Role> extractRoles(UserEntity.Roles roles) {
        return roles.values().stream()
                .map(role -> switch (role.toLowerCase()) {
                    case "admin" -> DefaultRole.ADMIN;
                    case "editor" -> DefaultRole.EDITOR;
                    case "viewer" -> DefaultRole.VIEWER;
                    default -> throw new UserRoleNotFoundException(role);
                })
                .collect(Collectors.toSet());
    }
}
