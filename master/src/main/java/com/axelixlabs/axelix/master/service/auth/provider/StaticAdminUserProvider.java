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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.axelixlabs.axelix.common.auth.core.ExternalAuthority;
import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.master.autoconfiguration.auth.StaticAdminCredentialsProperties;
import com.axelixlabs.axelix.master.exception.auth.UserNotFoundException;
import com.axelixlabs.axelix.master.service.auth.UserLoginService;

/**
 * {@link UserLoginService} that authenticates a given user by the static pair of the username/password.
 *
 * @author Mikhail Polivakha
 */
public class StaticAdminUserProvider implements UserProvider {

    private static final String ADMIN_ROLE = "ADMIN";

    private final StaticAdminCredentialsProperties staticCredentialsConfig;

    public StaticAdminUserProvider(StaticAdminCredentialsProperties staticCredentialsConfig) {
        this.staticCredentialsConfig = staticCredentialsConfig;
    }

    @Override
    public User load(String username) throws UserNotFoundException {
        if (Objects.equals(staticCredentialsConfig.getUsername(), username)) {
            return new DefaultUser(
                    username,
                    staticCredentialsConfig.getPassword(),
                    Set.of(new DefaultRole(
                            ADMIN_ROLE, Arrays.stream(ExternalAuthority.values()).collect(Collectors.toSet()))));
        } else {
            throw new UserNotFoundException(username);
        }
    }
}
