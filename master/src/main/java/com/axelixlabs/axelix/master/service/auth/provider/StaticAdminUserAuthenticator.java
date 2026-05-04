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

import org.jspecify.annotations.Nullable;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;
import com.axelixlabs.axelix.common.auth.core.DefaultUser;
import com.axelixlabs.axelix.common.auth.core.User;
import com.axelixlabs.axelix.common.utils.Assert;
import com.axelixlabs.axelix.master.autoconfiguration.auth.properties.StaticAdminCredentialsProperties;

/**
 * {@link UserAuthenticator} that authenticates a given user by the static pair of the username/password.
 *
 * @author Mikhail Polivakha
 */
public class StaticAdminUserAuthenticator implements UserAuthenticator {

    private final StaticAdminCredentialsProperties staticCredentialsConfig;

    public StaticAdminUserAuthenticator(StaticAdminCredentialsProperties staticCredentialsConfig) {
        Assert.notNull(staticCredentialsConfig.getUsername(), "username is required when static-admin is enabled");
        Assert.notNull(staticCredentialsConfig.getPassword(), "password is required when static-admin is enabled");
        this.staticCredentialsConfig = staticCredentialsConfig;
    }

    @Override
    public @Nullable User authenticate(String username, String password) {

        if (Objects.equals(staticCredentialsConfig.getUsername(), username)
                && Objects.equals(staticCredentialsConfig.getPassword(), password)) {
            return new DefaultUser(
                    staticCredentialsConfig.getUsername(),
                    staticCredentialsConfig.getPassword(),
                    Set.of(DefaultRole.SUPER_ADMIN));
        }

        return null;
    }
}
