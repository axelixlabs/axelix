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
package com.axelixlabs.axelix.master.autoconfiguration.auth.properties;

import org.jspecify.annotations.NullMarked;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

import com.axelixlabs.axelix.common.auth.core.DefaultRole;

import static com.axelixlabs.axelix.master.autoconfiguration.auth.SecurityAutoConfiguration.SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX;

/**
 * Configuration of the {@link DefaultRole#SUPER_ADMIN}.
 *
 * @author Mikhail Polivakha
 */
@NullMarked
@ConfigurationProperties(prefix = SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX)
public record SuperAdminConfigurationProperties(Credentials credentials) {

    public SuperAdminConfigurationProperties {
        Assert.notNull(
                credentials.username(),
                "The username for the super-admin is 'null'. Make sure the " + SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX
                        + ".credentials.username is specified correctly");

        Assert.notNull(
                credentials.password(),
                "The password for the super-admin is 'null'. Make sure the " + SUPER_ADMIN_LOGIN_PROPERTIES_PREFIX
                        + ".credentials.password is specified correctly");
    }

    public String getUsername() {
        return credentials.username();
    }

    public String getPassword() {
        return credentials.password();
    }

    /**
     * @param username super-admin username
     * @param password plain text or encoded password using DelegatingPasswordEncoder format
     * {@code {noop}}, {@code {bcrypt}}
     */
    record Credentials(String username, String password) {}
}
