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

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.Role;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.StaticAdminCredentialsProperties;
import com.axelixlabs.axelix.master.domain.UserEntity;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.service.state.UserManaged;

/**
 * {@link UserProvider} that authenticates a given user by the static pair of the username/password.
 *
 * @author Mikhail Polivakha
 */
public class StaticAdminUserProvider implements UserProvider {

    private final StaticAdminCredentialsProperties staticCredentialsConfig;
    private final UserManaged userManaged;

    public StaticAdminUserProvider(StaticAdminCredentialsProperties staticCredentialsConfig, UserManaged userManaged) {
        this.staticCredentialsConfig = staticCredentialsConfig;
        this.userManaged = userManaged;
    }

    @Override
    public User load(String username) throws UserNotFoundException {
        if (Objects.equals(staticCredentialsConfig.getUsername(), username)) {
            return new DefaultUser(
                    staticCredentialsConfig.getUsername(),
                    staticCredentialsConfig.getPassword(),
                    Set.of(DefaultRole.SUPER_ADMIN));
        }

        UserEntity user =
                userManaged.getUserByUsername(username).orElseThrow(() -> new UserNotFoundException(username));

        if (user.password() == null) {
            throw new UserNotFoundException(username);
        }

        return new DefaultUser(
                user.username(), user.password(), extractRoles(user.roles().values()));
    }

    private Set<Role> extractRoles(Set<String> roles) {
        return roles.stream()
                .map(role -> switch (role.toLowerCase()) {
                    case "admin" -> DefaultRole.ADMIN;
                    case "editor" -> DefaultRole.EDITOR;
                    default -> DefaultRole.VIEWER;
                })
                .collect(Collectors.toSet());
    }
}
