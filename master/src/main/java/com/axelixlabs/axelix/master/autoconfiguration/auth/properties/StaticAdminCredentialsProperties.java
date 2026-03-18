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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.Assert;

/**
 * Configuration of the static-admin.
 *
 * @author Mikhail Polivakha
 */
@SuppressWarnings("NullAway")
@ConfigurationProperties(prefix = "axelix.master.auth.options.static-admin")
public record StaticAdminCredentialsProperties(Credentials credentials) {

    public StaticAdminCredentialsProperties {
        Assert.notNull(
                credentials.username(),
                "The username for the static-admin is 'null'. Make sure the axelix.master.auth.static-admin.credentials.username is specified correctly");

        Assert.notNull(
                credentials.password(),
                "The password for the static-admin is 'null'. Make sure the axelix.master.auth.static-admin.credentials.password is specified correctly");
    }

    public String getUsername() {
        return credentials.username();
    }

    public String getPassword() {
        return credentials.password();
    }

    record Credentials(String username, String password) {}
}
